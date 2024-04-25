package com.ksudemo.hungrygoat.app.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.ksudemo.hungrygoat.R

class SettingsActivity : AppCompatActivity(), OnClickListener {
    private lateinit var drawGrahamScanLinesCheckBox: CheckBox
    private lateinit var drawDogBoundsCheckBox: CheckBox
    private lateinit var drawGoatBoundsCheckBox: CheckBox
    private lateinit var rangeSlider: Slider

    private lateinit var saveSettingButton: ImageButton
    private lateinit var settings: SharedPreferences

    private lateinit var drawGrahamScanLinesResString: String
    private lateinit var drawGoatBoundsResString: String
    private lateinit var drawDogBoundsResString: String
    private lateinit var changeObjectsSizeString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(applicationContext, StartActivity::class.java))
            }
        })

        drawGrahamScanLinesCheckBox = findViewById(R.id.drawGrahamScanLinesCheckBox)
        drawDogBoundsCheckBox = findViewById(R.id.drawDogBoundsCheckBox)
        drawGoatBoundsCheckBox = findViewById(R.id.drawGoatBoundsCheckBox)
        rangeSlider = findViewById(R.id.rangeSlider)

        drawGrahamScanLinesResString = resources.getString(R.string.enableGrahamScanLines)
        drawGoatBoundsResString = resources.getString(R.string.enableRenderGoatBounds)
        drawDogBoundsResString = resources.getString(R.string.enableRenderDogBounds)
        changeObjectsSizeString = resources.getString(R.string.changeObjectsSize)

        settings = applicationContext.getSharedPreferences(
            resources.getString(R.string.sharedPrefsSettingsName),
            Context.MODE_PRIVATE
        )

        drawGoatBoundsCheckBox.isChecked = settings.getBoolean(drawGoatBoundsResString, true)
        drawDogBoundsCheckBox.isChecked = settings.getBoolean(drawDogBoundsResString, true)
        drawGrahamScanLinesCheckBox.isChecked =
            settings.getBoolean(drawGrahamScanLinesResString, false)
        rangeSlider.value = settings.getFloat(changeObjectsSizeString, 40f)

        saveSettingButton = findViewById(R.id.saveSettingsButton)
        saveSettingButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveSettingsButton -> {
                Log.d("mytag", "${rangeSlider.value}")
                settings.edit().apply {
                    putBoolean(drawGoatBoundsResString, drawGoatBoundsCheckBox.isChecked)
                    putBoolean(drawDogBoundsResString, drawDogBoundsCheckBox.isChecked)
                    putBoolean(drawGrahamScanLinesResString, drawGrahamScanLinesCheckBox.isChecked)
                    putFloat(changeObjectsSizeString, rangeSlider.value)
                    apply()
                }
                Log.d(
                    "mytag",
                    "$drawGoatBoundsResString - ${drawGoatBoundsCheckBox.isChecked}\n" +
                            "$drawDogBoundsResString - ${drawDogBoundsCheckBox.isChecked}\n" +
                            "$drawGrahamScanLinesResString - ${drawGrahamScanLinesCheckBox.isChecked}\n" +
                            "$changeObjectsSizeString - ${rangeSlider.value}\n"
                )
                startActivity(
                    Intent(
                        applicationContext,
                        StartActivity::class.java
                    )
                )
            }

            else -> return
        }
    }
}
