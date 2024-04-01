@file:Suppress("SameParameterValue")

package com.example.hungrygoat.app.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.alertDialogs.DialogHelper
import com.example.hungrygoat.app.helpers.recyclerViewAdapters.RecyclerViewAdapter
import com.example.hungrygoat.constants.appContants.AppConstants
import com.example.hungrygoat.constants.appContants.LevelConditionInfo
import com.example.hungrygoat.constants.appContants.SingletonAppConstantsInfo
import com.example.hungrygoat.constants.appContants.educationLevelConditions

class LevelSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var countStarsTextView: TextView
    private lateinit var resetLevelCompletionImgButton: ImageButton
    private lateinit var exitButton: ImageButton

    private lateinit var appConstants: AppConstants

    private lateinit var levelsInfo: List<LevelConditionInfo>

    private lateinit var clickableButtons: List<Pair<IntRange, Int>>

    private var countAbuse = 0

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.level_selection_layout)
        Log.d("mytag", "level selection on create")

        supportActionBar?.setDisplayShowTitleEnabled(false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(applicationContext, StartActivity::class.java))
                appConstants.getEngine().clearObjects()
            }
        })

        exitButton = findViewById(R.id.closeAppButton)
        resetLevelCompletionImgButton = findViewById(R.id.resetLevelCompletionButton)
        countStarsTextView = findViewById(R.id.countStarsTextView)
        appConstants = SingletonAppConstantsInfo.getAppConst()

        levelsInfo = appConstants.levelsInfo
    }


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onResume() {
        super.onResume()

        fun createDialog(text: String) {
            val b = Bundle().apply {
                putString("text", text)
                putBoolean("hideExitButton", true)
            }
            val neg = ResourcesCompat.getDrawable(resources, R.mipmap.exit, theme)!!
            val pos = ResourcesCompat.getDrawable(resources, R.mipmap.tick, theme)!!
            DialogHelper().createDialog(this, b, neg, pos, { countAbuse = 0 }, {})
        }

        fun createToast(text: String) =
            Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()

        Log.d("mytag", "level selection on resume")
        val countStars = levelsInfo.sumOf { it.rating } / 2f // half of the star is 1 point
        val totalStars = levelsInfo.size * 3 // levels size * 3 stars

        countStarsTextView.text = "$countStars/$totalStars"

        clickableButtons = SingletonAppConstantsInfo.getAppConst().needStarsToUnlockLevels

        val adapter = RecyclerViewAdapter(resources)
        adapter.setData(levelsInfo, clickableButtons, countStars)
        adapter.setOnItemClickListener { position ->
            setAdapterOnClick(position)
        }
        adapter.setOnItemLongClickListener { position ->
            setAdapterOnLongClick(position)
        }


        countStarsTextView.setOnClickListener {
            if (countAbuse++ == 10) {
                appConstants.abuseRating()
                createToast("Просто перезайдите в игру")
            }
            Log.d("mytag", "$countAbuse")
        }

        exitButton.setOnClickListener {
            val intent = Intent(applicationContext, StartActivity::class.java)
            startActivity(intent)
        }

        resetLevelCompletionImgButton.setOnClickListener {
            resetStars()
            getSharedPreferences("noteAbautDogs", Context.MODE_PRIVATE).edit()
                .putBoolean("isShown", false).apply()
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, 3)
            setHasFixedSize(true)
            this.adapter = adapter
        }
    }


    @SuppressLint("SetTextI18n")
    private fun resetStars() {
        val countStars = levelsInfo.sumOf { it.rating } / 2f // half of the star is 1 point
        val totalStars = levelsInfo.size * 3 // levels size * 3 stars

        val posClickListener = {
            val adapter = RecyclerViewAdapter(resources)
            adapter.setOnItemClickListener { position ->
                setAdapterOnClick(position)
            }
            adapter.setOnItemLongClickListener { position ->
                setAdapterOnLongClick(position)
            }

            appConstants.resetLevelsMap()
            levelsInfo = appConstants.levelsInfo
            countStarsTextView.text = "0/$totalStars"
            adapter.setData(levelsInfo, clickableButtons, countStars)
            recyclerView.apply {
                layoutManager = GridLayoutManager(applicationContext, 3)
                setHasFixedSize(true)
            }
            recyclerView.adapter = adapter

            startActivity(Intent(applicationContext, LevelSelectionActivity::class.java))
        }

        val neg = ResourcesCompat.getDrawable(
            resources, R.mipmap.exit, theme
        )!!
        val pos = ResourcesCompat.getDrawable(
            resources, R.mipmap.tick, theme
        )!!
        val bundle = Bundle()
        bundle.putString("text", "Вы уверены, что хотите сбросить прохождение уровней?")
        bundle.putBoolean("hideExitButton", true)

        DialogHelper().createDialog(
            this,
            bundle,
            neg, pos,
            {}, posClickListener
        )
    }

    private fun setAdapterOnClick(position: Int) {
        val countStars = levelsInfo.sumOf { it.rating } / 2f // half of the star is 1 point

        val curPair = clickableButtons.find { it.first.contains(position) }
        val canClick = countStars >= (curPair?.second ?: 0)

        if (!canClick) {
            Toast.makeText(
                applicationContext,
                "Наберите ещё звезд: ${(curPair?.second ?: 0) - countStars}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val levelCondition = levelsInfo[position].levelCondition

        val extras = Bundle()

        extras.putSerializable("levelCondition", levelCondition)
        extras.putSerializable("caller", LevelSelectionActivity::class.java)


        val intent = if (educationLevelConditions.contains(levelCondition)) {
            Intent(applicationContext, EducationActivity::class.java).apply {
                putExtra("eduCondition", levelCondition.toString())
            }

        } else Intent(applicationContext, LevelActivity::class.java).apply {
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

    private fun setAdapterOnLongClick(position: Int) {
        Toast.makeText(
            applicationContext,
            levelsInfo[position].levelConditionTranslated,
            Toast.LENGTH_SHORT
        ).show()
        Log.d("mytag", "${levelsInfo[position]}")
    }
}
