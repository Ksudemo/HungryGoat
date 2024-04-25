package com.ksudemo.hungrygoat.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.ksudemo.hungrygoat.R

class RulesActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rules_layout)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(applicationContext, StartActivity::class.java))
            }
        })

        val gameRulesTextView1: TextView = findViewById(R.id.gameRulesTextView1)
        val gameRulesTextView2: TextView = findViewById(R.id.gameRulesTextView2)
        val gameRulesTextView3: TextView = findViewById(R.id.gameRulesTextView3)
        gameRulesTextView1.text =
            "Козы очень прожорливые животные, они съедают всю траву, до которой могут дотянуться." +
                    " Козу можно привязать при помощи колышков и веревок так, чтобы она съела определённую фигуру.\n\n" +
                    " Если привязать козу к одному колышку, получится круг.\n"

        gameRulesTextView2.text =
            "\n Если натянуть на лугу веревку между двумя колышками, а " +
                    "у второй веревки один конец привязать к ошейнику козы, и " +
                    "на втором сделать петлю, свободно скользящую по" +
                    "натянутой веревке, то получится следующая фигура, напоминающая стадиончик."

        gameRulesTextView3.text =
            "Иногда, чтобы предотвратить побег козы, используют собак." +
                    " Их можно привязывать так же, как и коз.\n" +
                    " Коза боится собак и избегает мест, где её могут достать собаки, но," +
                    " чтобы коза не осталась голодной, собак тоже держат на привязи.\n\n"
    }
}
