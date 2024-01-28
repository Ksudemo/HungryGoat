package com.example.hungrygoat.app.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hungrygoat.R
import com.example.hungrygoat.app.helpers.CustomAdapter

class SettingsActivity : AppCompatActivity(), OnClickListener {


    private lateinit var pickCellSizeSpinner: Spinner

    //    private lateinit var pickRenderTypeSpinner: Spinner
    private lateinit var cellIndexCheckBox: CheckBox
    private lateinit var drawRopeNodesCheckBox: CheckBox
    private lateinit var drawDogBoundsCheckBox: CheckBox
    private lateinit var drawGoatBoundsCheckBox: CheckBox

    private lateinit var saveSettingButton: Button
    private lateinit var settings: SharedPreferences

    private lateinit var s1: String
    private lateinit var s2: String
    private lateinit var s3: String
    private lateinit var s4: String
    private lateinit var s5: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        cellIndexCheckBox = findViewById(R.id.cellIndexCheckBox)
        drawRopeNodesCheckBox = findViewById(R.id.drawRopeNodesCheckBox)
        drawDogBoundsCheckBox = findViewById(R.id.drawDogBoundsCheckBox)
        drawGoatBoundsCheckBox = findViewById(R.id.drawGoatBoundsCheckBox)

        s1 = resources.getString(R.string.pick_cell_size)
        s2 = resources.getString(R.string.enableDrawCellIndex)
        s3 = resources.getString(R.string.enableDrawRopeNodes)
        s4 = resources.getString(R.string.enableRenderGoatBounds)
        s5 = resources.getString(R.string.enableRenderDogBounds)

        settings = applicationContext.getSharedPreferences(
            resources.getString(R.string.sharedPrefsSettingsName),
            Context.MODE_PRIVATE
        )

        cellIndexCheckBox.isChecked = settings.getBoolean(s2, false)
        drawRopeNodesCheckBox.isChecked = settings.getBoolean(s3, false)
        drawGoatBoundsCheckBox.isChecked = settings.getBoolean(s4, false)
        drawDogBoundsCheckBox.isChecked = settings.getBoolean(s5, false)

        saveSettingButton = findViewById(R.id.saveSettingsButton)
        saveSettingButton.setOnClickListener(this)


        val cellSizesList = mutableListOf(
            "Выберите элемент",
            "200.0f",
            "100.0f",
            "50.0f",
            "20.0f",
            "15.0f",
            "10.0f",
            "9.0f",
            "8.0f",
            "7.0f",
            "6.0f",
            "5.0f",
            "4.0f",
            "3.0f",
            "2.0f",
            "1.0f"
        )

        pickCellSizeSpinner = findViewById(R.id.pickCellSizeSpinner)

        val cellSizeAdapter = CustomAdapter(
            this,
            android.R.layout.simple_spinner_item,
            cellSizesList
        )


        cellSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pickCellSizeSpinner.adapter = cellSizeAdapter
        pickCellSizeSpinner.setSelection(
            getItemToSelect(
                cellSizesList,
                settings.getFloat(s1, -1f).toString()
            )
        )
    }

    private fun getItemToSelect(lst: List<String>, itemToSearch: String): Int {
        lst.forEachIndexed { i, str ->
            if (str.contains(itemToSearch)) return i
        }
        return -1
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveSettingsButton -> {

                val cellSizeSpinnerItem = pickCellSizeSpinner.selectedItem.toString()

                if (cellSizeSpinnerItem == "Выберите элемент") {
                    Toast.makeText(
                        applicationContext,
                        "Выберите размер клетки!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                settings.edit().apply {
                    putFloat(s1, cellSizeSpinnerItem.split("f").first().toFloat())
                    putBoolean(s2, cellIndexCheckBox.isChecked)
                    putBoolean(s3, drawRopeNodesCheckBox.isChecked)
                    putBoolean(s4, drawGoatBoundsCheckBox.isChecked)
                    putBoolean(s5, drawDogBoundsCheckBox.isChecked)
                    apply()
                }

                startActivity(
                    Intent(
                        applicationContext,
                        StartActivity::class.java
                    )
                )
            }

            else -> {
                return
            }
        }
    }
}
