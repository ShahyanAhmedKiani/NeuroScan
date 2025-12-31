package com.example.neuroscan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class MemoryBoardAdapter(
    private val memoryCards: List<MemoryCard>,
    private val onCardClicked: (Int) -> Unit
) : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = memoryCards.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView as MaterialCardView
        private val imageView: ImageView = itemView.findViewById(R.id.ivMemoryCard)

        fun bind(position: Int) {
            val memoryCard = memoryCards[position]
            if (memoryCard.isFaceUp) {
                imageView.setImageResource(memoryCard.identifier)
                card.isClickable = false
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background) // Placeholder for the back of the card
                card.isClickable = true
            }

            card.setOnClickListener {
                onCardClicked(position)
            }
        }
    }
}
