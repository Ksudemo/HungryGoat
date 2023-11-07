package com.example.hungrygoat.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.RecyclerViewAdapter

class LevelSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_selection_layout)

        toolbar = findViewById(R.id.levelSelectionToolbar)
        setSupportActionBar(toolbar)

        val levelsList = mutableListOf(
            Pair("Круг", true),
            Pair("Квадрат", true),
            Pair("Овал", false),
            Pair("Треугольник", false)
        )

        val adapter = RecyclerViewAdapter(levelsList)
        adapter.setOnItemClickListener(object : RecyclerViewAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                val levelCondition = levelsList[position].first

                val extras = Bundle()

                extras.putString("levelCondition", levelCondition)
                extras.putSerializable("caller", LevelSelectionActivity::class.java)

                val intent = Intent(applicationContext, LevelActivity::class.java).apply {
                    putExtras(extras)
                }

                startActivity(intent)
            }
        })

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
