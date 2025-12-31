package com.example.neuroscan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class PdfReportGenerator(private val context: Context) {

    fun createPdf(scanResult: ScanResult, bitmap: Bitmap) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val purpleColor = ContextCompat.getColor(context, R.color.purple_accent)

        // 1. App Title
        val titlePaint = Paint().apply {
            color = purpleColor
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("NeuroScan Report", pageInfo.pageWidth / 2f, 50f, titlePaint)

        // 2. Scan Image Section
        val sectionTitlePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Scan Image", 40f, 100f, sectionTitlePaint)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
        val imageX = (pageInfo.pageWidth - scaledBitmap.width) / 2f
        canvas.drawBitmap(scaledBitmap, imageX, 120f, null)

        // 3. Detection Analysis Section
        val contentYStart = 120f + scaledBitmap.height + 40f
        canvas.drawText("Detection Analysis", 40f, contentYStart, sectionTitlePaint)

        val resultPaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 14f
        }
        val resultLines = scanResult.resultText.split("\n")
        var y = contentYStart + 25f
        resultLines.forEach {
            canvas.drawText(it, 40f, y, resultPaint)
            y += 20f
        }

        // 4. General Information Section
        y += 20f // Add extra space
        canvas.drawText("General Information", 40f, y, sectionTitlePaint)
        y += 25f

        val tumorType = resultLines.firstOrNull()?.substringBefore(" - ") ?: "Unknown"
        val tumorInfo = getTumorInformation(tumorType)
        val infoPaint = Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 12f
        }

        // Manual text wrapping
        val textWidth = pageInfo.pageWidth - 80 // 40f margin on each side
        var currentLine = ""
        for (word in tumorInfo.split(" ")) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (infoPaint.measureText(testLine) < textWidth) {
                currentLine = testLine
            } else {
                canvas.drawText(currentLine, 40f, y, infoPaint)
                y += 15f
                currentLine = word
            }
        }
        canvas.drawText(currentLine, 40f, y, infoPaint) // Draw the last line

        document.finishPage(page)

        savePdf(document)
    }

    private fun savePdf(document: PdfDocument) {
        val fileName = "NeuroScan_Report_${System.currentTimeMillis()}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        val fos = FileOutputStream(file)
        document.writeTo(fos)
        document.close()
        fos.close()

        val contentValues = android.content.ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
    }

    private fun getTumorInformation(tumorType: String): String {
        return when (tumorType) {
            "Glioma" -> "A glioma is a type of tumor that starts in the glial cells of the brain or the spine. It is the most common type of primary brain tumor. Glial cells are the supporting cells of the brain."
            "Meningioma" -> "A meningioma is a tumor that arises from the meninges — the membranes that surround your brain and spinal cord. Most meningiomas are non-cancerous (benign), though, rarely, a meningioma may be cancerous (malignant)."
            "Pituitary" -> "A pituitary adenoma is a benign tumor of the pituitary gland. The pituitary gland is a small, bean-shaped gland situated at the base of your brain which is responsible for producing several key hormones."
            "No tumor detected" -> "No abnormalities were detected in the scan. This is a positive result, but regular check-ups are always recommended."
            else -> "No specific information available for this scan result."
        }
    }
}
