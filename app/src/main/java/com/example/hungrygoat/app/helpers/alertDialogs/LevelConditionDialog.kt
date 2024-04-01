package com.example.hungrygoat.app.helpers.alertDialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.hungrygoat.R
import com.example.hungrygoat.app.activities.LevelSelectionActivity

class LevelConditionDialog(
    private val levelCondMipmapID: Int,
    private val levelCond: String,
    private val posClickListener: GameDialog.OnClickListener
) : DialogFragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.ProgressDialogTheme)
        val inflater = requireActivity().layoutInflater
        val layoutResourseId = R.layout.level_cond_dialog_layout

        val dialogView = inflater.inflate(layoutResourseId, null)

        val levelCondDialogTextView: TextView =
            dialogView.findViewById(R.id.levelCondDialogTextView)

        val positiveButton: ImageButton = dialogView.findViewById(R.id.positiveButton)
        val exitButton: ImageButton = dialogView.findViewById(R.id.exitButton)
        val levelConditionImageView: ImageView =
            dialogView.findViewById(R.id.levelConditionImageView)

        levelCondDialogTextView.text = levelCond.uppercase()

        exitButton.setOnClickListener {
            val intent = Intent(context, LevelSelectionActivity::class.java)
            startActivity(intent)
        }
        positiveButton.setOnClickListener {
            posClickListener.onClick()
            dismiss()
        }
        levelConditionImageView.setImageResource(levelCondMipmapID)
        builder.setView(dialogView)
        return builder.create()
    }
}