package com.example.hungrygoat.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.RecyclerViewAdapter
import com.example.hungrygoat.constants.AppConstants
import com.example.hungrygoat.constants.SingletonAppConstantsInfo

class LevelSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    private lateinit var appConstants: AppConstants
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_selection_layout)

        toolbar = findViewById(R.id.levelSelectionToolbar)
        setSupportActionBar(toolbar)

        appConstants = SingletonAppConstantsInfo.getAppConst()

        val adapter = RecyclerViewAdapter(appConstants.translatedList)
        adapter.setOnItemClickListener { position ->
            val levelCondition = appConstants.levelsList[position].first

            val extras = Bundle()

            extras.putSerializable("levelCondition", levelCondition)
            extras.putSerializable("caller", LevelSelectionActivity::class.java)

            val intent = Intent(applicationContext, LevelActivity::class.java).apply {
                putExtras(extras)
            }

            val lastLevelPlayedSharedPrefsStr =
                resources.getString(R.string.sharedPrefsSettingsName)
            val lastLevelPlayedStr = resources.getString(R.string.lastLevelPlayed)

            val lastLevelSharedPref =
                applicationContext.getSharedPreferences(
                    lastLevelPlayedSharedPrefsStr,
                    Context.MODE_PRIVATE
                )

            lastLevelSharedPref.edit().apply {
                putInt(lastLevelPlayedStr, position)
                apply()
            }

            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
