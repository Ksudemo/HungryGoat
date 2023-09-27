package com.example.hungrygoat.app.helpers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomAdapter(
    private val context: Context, private val textViewResourceId: Int,
    private var objects: List<String>,
) : ArrayAdapter<String>(context, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v =
            convertView
                ?: View.inflate(
                    context,
                    textViewResourceId, null
                )
        val tv = v as TextView?
        tv!!.text = objects[position]
        tv.textSize = 20f
        return v!!
    }
}