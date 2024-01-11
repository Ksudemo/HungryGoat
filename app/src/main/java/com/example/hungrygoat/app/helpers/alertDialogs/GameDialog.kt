package com.example.hungrygoat.app.helpers.alertDialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.hungrygoat.app.helpers.alertDialogs.GameDialog.OnClickListener

class GameDialog : DialogFragment() {
    fun interface OnClickListener {
        fun onClick()
    }

    private var onPosClickListener = OnClickListener { }
    private var onNegClickListener = OnClickListener { }
    fun setOnClickListener(negClickListener: OnClickListener, posClickListener: OnClickListener) {
        onNegClickListener = negClickListener
        onPosClickListener = posClickListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(arguments?.getString("title"))
                .setMessage(arguments?.getString("text"))
                .setPositiveButton(arguments?.getString("posButton")) { _, _ ->
                    onPosClickListener.onClick()
                }
                .setNegativeButton(arguments?.getString("negButton")) { _, _ ->
                    onNegClickListener.onClick()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}