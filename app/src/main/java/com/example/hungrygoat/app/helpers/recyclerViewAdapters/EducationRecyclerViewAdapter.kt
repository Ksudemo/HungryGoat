package com.example.hungrygoat.app.helpers.recyclerViewAdapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.recyclerViewAdapters.EducationRecyclerViewAdapter.OnItemClickListener
import com.example.hungrygoat.constants.appContants.LevelConditionInfo

class EducationRecyclerViewAdapter(
    private val rsc: Resources,
    private val data: List<Pair<LevelConditionInfo?, Int>>
) :
    RecyclerView.Adapter<EducationRecyclerViewAdapter.MyViewHolder>() {

    fun interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var clickListener: OnItemClickListener = OnItemClickListener { }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        clickListener = listener
    }

    class MyViewHolder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val levelConditionButton: ImageButton = itemView.findViewById(R.id.levelConditionButton)

        init {
            levelConditionButton.setOnClickListener { listener.onItemClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.education_level_select_item, parent, false)


        return MyViewHolder(itemView, clickListener)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.levelConditionButton.text = data[position].second
        holder.levelConditionButton.setImageResource(data[position].second)
    }
}