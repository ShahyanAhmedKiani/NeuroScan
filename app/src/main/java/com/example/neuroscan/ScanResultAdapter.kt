package com.example.neuroscan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScanResultAdapter(private val scanResults: List<ScanResult>) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scan_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scanResult = scanResults[position]
        holder.bind(scanResult)
    }

    override fun getItemCount() = scanResults.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_scan_image)
        private val resultView: TextView = itemView.findViewById(R.id.tv_scan_result)
        private val timestampView: TextView = itemView.findViewById(R.id.tv_scan_timestamp)
        private val downloadButton: Button = itemView.findViewById(R.id.btn_download_report)

        fun bind(scanResult: ScanResult) {
            resultView.text = scanResult.resultText

            // Format and display the timestamp
            val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            val date = Date(scanResult.timestamp)
            timestampView.text = sdf.format(date)

            val imageBytes = Base64.decode(scanResult.imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)

            imageView.setOnClickListener {
                val context = itemView.context
                val fileName = "temp_image.png"
                val file = File(context.cacheDir, fileName)

                try {
                    val stream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    return@setOnClickListener
                }

                val intent = Intent(context, FullScreenImageActivity::class.java).apply {
                    putExtra("imagePath", file.absolutePath)
                }
                context.startActivity(intent)
            }

            downloadButton.setOnClickListener {
                val context = itemView.context
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Storage permission is required to save reports.", Toast.LENGTH_SHORT).show()
                    // In a real app, you would request the permission here.
                } else {
                    val pdfGenerator = PdfReportGenerator(context)
                    pdfGenerator.createPdf(scanResult, bitmap)
                    Toast.makeText(context, "Report saved to Downloads folder.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
