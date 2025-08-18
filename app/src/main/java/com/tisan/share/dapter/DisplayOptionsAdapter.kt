package com.tisan.share.dapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kj.infinite.R
import com.tisan.share.data.DisplayOption

class DisplayOptionsAdapter(
    private val options: List<DisplayOption>,
    private val onItemClick: (DisplayOption) -> Unit
) : RecyclerView.Adapter<DisplayOptionsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.iconImageView)
        val label: TextView = view.findViewById(R.id.labelTextView)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(options[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_display_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = options[position]
        holder.icon.setImageResource(item.iconRes)
        holder.label.text = item.label
    }

    override fun getItemCount() = options.size
}
