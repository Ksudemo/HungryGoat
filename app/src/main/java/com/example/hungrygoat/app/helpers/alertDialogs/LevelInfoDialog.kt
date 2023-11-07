package com.example.hungrygoat.app.helpers.alertDialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class LevelInfoDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            println(arguments)
            builder.setTitle(arguments?.getString("title"))
                .setMessage(arguments?.getString("text"))
                .setPositiveButton(arguments?.getString("posButton")) { dialog, id ->
                    dialog.cancel()
                }
                .setNegativeButton(arguments?.getString("negButton")) { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}