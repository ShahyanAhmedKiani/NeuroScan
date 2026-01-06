package com.example.neuroscan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScanResultAdapter(private val scanResults: MutableList<ScanResult>) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scan_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(scanResults[position])
    }

    override fun getItemCount() = scanResults.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_scan_image)
        private val resultView: TextView = itemView.findViewById(R.id.tv_scan_result)
        private val timestampView: TextView = itemView.findViewById(R.id.tv_scan_timestamp)
        private val downloadButton: Button = itemView.findViewById(R.id.btn_download_report)
        private val deleteButton: Button = itemView.findViewById(R.id.btn_delete)

        fun bind(scanResult: ScanResult) {
            resultView.text = scanResult.resultText

            val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
            timestampView.text = sdf.format(Date(scanResult.timestamp))

            val imageBytes = Base64.decode(scanResult.imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)

            imageView.setOnClickListener { /* Full-screen logic */ }

            downloadButton.setOnClickListener { handleDownload(scanResult, bitmap) }

            deleteButton.setOnClickListener { handleDelete(scanResult) }
        }

        private fun handleDownload(scanResult: ScanResult, bitmap: Bitmap) {
            val context = itemView.context
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Storage permission is required.", Toast.LENGTH_SHORT).show()
                return
            }

            val progressDialog = AlertDialog.Builder(context)
                .setTitle("Generating Report")
                .setMessage("Please wait...")
                .setCancelable(false)
                .show()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    PdfReportGenerator(context).createPdf(scanResult, bitmap)
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(context, "Report saved to Downloads folder.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(context, "Failed to save report: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        private fun handleDelete(scanResult: ScanResult) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Scan")
                .setMessage("Are you sure you want to permanently delete this scan result?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteScanFromDatabase(scanResult)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun deleteScanFromDatabase(scanResult: ScanResult) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null || scanResult.key == null) {
                Toast.makeText(itemView.context, "Error: Could not delete scan.", Toast.LENGTH_SHORT).show()
                return
            }

            FirebaseDatabase.getInstance().getReference("scans").child(userId).child(scanResult.key!!)
                .removeValue()
                .addOnSuccessListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        scanResults.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    Toast.makeText(itemView.context, "Scan deleted.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { 
                    Toast.makeText(itemView.context, "Failed to delete scan.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
