package com.example.hungrygoat.app.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.hungrygoat.R

@Suppress("ClassName")
class RecyclerViewAdapter(private val levels: List<Pair<String, Boolean>>) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    private var clickListener: onItemClickListener = object : onItemClickListener {}

    interface onItemClickListener {
        fun onItemClick(position: Int) {

        }
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        clickListener = listener
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val levelDoneCheckBox: CheckBox = itemView.findViewById(R.id.levelDoneCheckBox)
        val playLevelButton: Button = itemView.findViewById(R.id.playLevelButton)

        init {
            playLevelButton.setOnClickListener { listener.onItemClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_item, parent, false)

        return MyViewHolder(itemView, clickListener)
    }

    override fun getItemCount(): Int = levels.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        levels[position].apply {
            holder.playLevelButton.text = first
            holder.levelDoneCheckBox.isChecked = second
        }

        holder
            .playLevelButton
//            .isClickable = if (position - 1 > 0) levels[position - 1].second else true
    }
}