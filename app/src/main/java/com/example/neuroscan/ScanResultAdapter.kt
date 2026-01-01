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
        val scanResult = scanResults[position]
        holder.bind(scanResult)
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
            val date = Date(scanResult.timestamp)
            timestampView.text = sdf.format(date)

            val imageBytes = Base64.decode(scanResult.imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)

            imageView.setOnClickListener {
                // Full-screen image logic... 
            }

            downloadButton.setOnClickListener {
                 // PDF download logic... 
            }

            deleteButton.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Scan")
                    .setMessage("Are you sure you want to permanently delete this scan result?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteScan(scanResult)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        private fun deleteScan(scanResult: ScanResult) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null || scanResult.key == null) {
                Toast.makeText(itemView.context, "Error: Could not delete scan.", Toast.LENGTH_SHORT).show()
                return
            }

            FirebaseDatabase.getInstance().getReference("scans").child(userId).child(scanResult.key!!)
                .removeValue()
                .addOnSuccessListener {
                    // Remove from the list and notify the adapter
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
