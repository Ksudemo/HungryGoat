package com.example.hungrygoat.app.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.RecyclerViewAdapter
import com.example.hungrygoat.app.helpers.alertDialogs.GameDialog
import com.example.hungrygoat.constants.AppConstants
import com.example.hungrygoat.constants.SingletonAppConstantsInfo

class LevelSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var resetLevelCompletionImgButton: ImageButton

    private lateinit var appConstants: AppConstants

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_selection_layout)

        resetLevelCompletionImgButton = findViewById(R.id.resetLevelCompletionButton)
        toolbar = findViewById(R.id.levelSelectionToolbar)
        setSupportActionBar(toolbar)

        appConstants = SingletonAppConstantsInfo.getAppConst()

        val adapter = RecyclerViewAdapter()
        adapter.setData(appConstants.levelsMap)
        adapter.setOnItemClickListener { position ->
            setAdapterOnClick(position)
        }
        resetLevelCompletionImgButton.setOnClickListener {

            val posClickListener = {
                appConstants.resetLevelsMap()
                adapter.setData(appConstants.levelsMap)
                adapter.notifyDataSetChanged()
            }
            createDialog(
                "Сбросить прохождение уровней",
                "Вы уверены, что хотите сбросить прохождение уровней?",
                "Нет",
                "Да",
                {}, posClickListener
            )
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setAdapterOnClick(position: Int) {
        val levelCondition = appConstants.levelsMap.toList()[position].first.levelCondition

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


    private fun createDialog(
        titleString: String,
        textString: String,
        negString: String,
        posString: String,
        negClickListener: GameDialog.OnClickListener,
        posClickListener: GameDialog.OnClickListener,
    ) {
        val dialog = GameDialog()
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()

        val bundle = Bundle()
        bundle.putString("title", titleString)
        bundle.putString("text", textString)
        bundle.putString("posButton", posString)
        bundle.putString("negButton", negString)

        dialog.setOnClickListener(negClickListener, posClickListener)

        dialog.arguments = bundle
        dialog.show(manager, "MyDialog")
        transaction.commit()
    }
}
