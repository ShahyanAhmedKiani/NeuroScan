package com.example.neuroscan

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

data class DetectionResult(val boundingBox: RectF, val label: String, val score: Float)

class ScanTumor : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var captureImageBtn: Button
    private lateinit var selectImageBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var resultTv: TextView
    private var imageUri: Uri? = null

    private var interpreter: Interpreter? = null
    private val labels = listOf("Glioma", "Meningioma", "No Tumor", "Pituitary")

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var selectImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_tumor)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        imageView = findViewById(R.id.imageView)
        captureImageBtn = findViewById(R.id.captureImageBtn)
        selectImageBtn = findViewById(R.id.selectImageBtn)
        saveBtn = findViewById(R.id.saveBtn)
        resultTv = findViewById(R.id.resultTv)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        captureImageBtn.isEnabled = false
        selectImageBtn.isEnabled = false

        setupLaunchers()

        captureImageBtn.setOnClickListener { checkCameraPermissionAndOpenCamera() }
        selectImageBtn.setOnClickListener { openGallery() }
        saveBtn.setOnClickListener { saveScanToDatabase() }

        // Initialize the Interpreter
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val model: ByteBuffer = FileUtil.loadMappedFile(this@ScanTumor, "model.tflite")
                interpreter = Interpreter(model)
                withContext(Dispatchers.Main) {
                    captureImageBtn.isEnabled = true
                    selectImageBtn.isEnabled = true
                    Toast.makeText(this@ScanTumor, "Model loaded successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScanTumor, "Error loading model", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun setupLaunchers() {
        requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openCamera() else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUri?.let { uri -> uriToBitmap(uri)?.let { runObjectDetection(it) } }
            }
        }

        selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                uriToBitmap(it)?.let { runObjectDetection(it) }
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        imageUri = mediaStoreHack()
        imageUri?.let { takePictureLauncher.launch(it) }
    }

    private fun openGallery() = selectImageLauncher.launch("image/*")

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun runObjectDetection(bitmap: Bitmap) {
        if (interpreter == null) {
            Toast.makeText(this, "Model not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val inputTensor = interpreter!!.getInputTensor(0)
            val inputShape = inputTensor.shape()
            val inputHeight = inputShape[1]
            val inputWidth = inputShape[2]

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f))
                .build()

            var tensorImage = TensorImage(inputTensor.dataType())
            tensorImage.load(argbBitmap)
            tensorImage = imageProcessor.process(tensorImage)

            val outputBuffer = Array(1) { Array(8) { FloatArray(8400) } }
            interpreter?.run(tensorImage.buffer, outputBuffer)

            val allDetections = mutableListOf<DetectionResult>()
            val output = outputBuffer[0]
            for (i in 0 until 8400) {
                val cx = output[0][i]
                val cy = output[1][i]
                val w = output[2][i]
                val h = output[3][i]

                val classProbabilities = floatArrayOf(output[4][i], output[5][i], output[6][i], output[7][i])
                var maxScore = 0f
                var maxIndex = -1
                for (j in classProbabilities.indices) {
                    if (classProbabilities[j] > maxScore) {
                        maxScore = classProbabilities[j]
                        maxIndex = j
                    }
                }

                if (maxScore > 0.5f) {
                    val label = labels[maxIndex]
                    if (label != "No Tumor") {
                        val left = (cx - w / 2) * argbBitmap.width
                        val top = (cy - h / 2) * argbBitmap.height
                        val right = (cx + w / 2) * argbBitmap.width
                        val bottom = (cy + h / 2) * argbBitmap.height

                        val boundingBox = RectF(left, top, right, bottom)
                        allDetections.add(DetectionResult(boundingBox, label, maxScore))
                    }
                }
            }

            val finalResults = nonMaxSuppression(allDetections)

            val resultToDisplay = if (finalResults.isEmpty()) {
                "No tumor detected"
            } else {
                formatResults(finalResults)
            }
            val imgWithResult = drawDetectionResult(argbBitmap, finalResults)

            withContext(Dispatchers.Main) {
                resultTv.text = resultToDisplay
                imageView.setImageBitmap(imgWithResult)
            }
        }
    }

    private fun nonMaxSuppression(
        allDetections: List<DetectionResult>,
        iouThreshold: Float = 0.4f
    ): List<DetectionResult> {
        val sortedDetections = allDetections.sortedByDescending { it.score }
        val finalDetections = mutableListOf<DetectionResult>()

        for (detection in sortedDetections) {
            var shouldAdd = true
            for (finalDetection in finalDetections) {
                if (iou(detection.boundingBox, finalDetection.boundingBox) > iouThreshold) {
                    shouldAdd = false
                    break
                }
            }
            if (shouldAdd) {
                finalDetections.add(detection)
            }
        }
        return finalDetections
    }

    private fun iou(box1: RectF, box2: RectF): Float {
        val x1 = max(box1.left, box2.left)
        val y1 = max(box1.top, box2.top)
        val x2 = min(box1.right, box2.right)
        val y2 = min(box1.bottom, box2.bottom)
        val intersectionArea = max(0f, x2 - x1) * max(0f, y2 - y1)
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    private fun formatResults(results: List<DetectionResult>): String {
        return results.joinToString("\n") {
            val width = it.boundingBox.width().toInt()
            val height = it.boundingBox.height().toInt()
            "${it.label} - ${String.format("%.2f", it.score)} (W: $width, H: $height, Area: ${width * height} px)"
        }
    }

    private fun drawDetectionResult(
        bitmap: Bitmap,
        detectionResults: List<DetectionResult>
    ): Bitmap {
        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)
        val paint = Paint()
        paint.textAlign = Paint.Align.LEFT

        for (result in detectionResults) {
            paint.color = Color.RED
            paint.strokeWidth = 8F
            paint.style = Paint.Style.STROKE
            val box = result.boundingBox
            canvas.drawRect(box, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.textSize = 40F
            val label = "${result.label} ${String.format("%.2f", result.score)}"
            canvas.drawText(label, box.left, box.top, paint)
        }
        return outputBitmap
    }

    private fun bitmapToBase64(bitmap: Bitmap): String? {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveScanToDatabase() {
        val drawable = imageView.drawable as? BitmapDrawable
        val bitmap = drawable?.bitmap

        if (bitmap == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
            return
        }

        val resultText = resultTv.text.toString()
        if (resultText.isEmpty() || resultText == "Result") {
            Toast.makeText(this, "No result to save", Toast.LENGTH_SHORT).show()
            return
        }

        val imageBase64 = bitmapToBase64(bitmap)
        if (imageBase64 == null) {
            Toast.makeText(this, "Failed to encode image.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You must be logged in to save results.", Toast.LENGTH_SHORT).show()
            return
        }

        val scanData = ScanResult(imageBase64, resultText, System.currentTimeMillis())

        database.getReference("scans").child(userId).push().setValue(scanData)
            .addOnSuccessListener {
                Toast.makeText(this, "Scan saved successfully.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save scan.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mediaStoreHack(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New_Scan_${System.currentTimeMillis()}")
            put(MediaStore.Images.Media.DESCRIPTION, "From NeuroScan Camera")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }
}
