package com.ksudemo.hungrygoat.app.helpers.alertDialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.ksudemo.hungrygoat.R
import com.ksudemo.hungrygoat.app.activities.LevelSelectionActivity
import com.ksudemo.hungrygoat.app.helpers.alertDialogs.GameDialog.OnClickListener

class GameDialog(
    private val rating: Int = -1,
    private val negDrawable: Drawable?,
    private val posDrawable: Drawable
) : DialogFragment() {

    fun interface OnClickListener {
        fun onClick()
    }

    private var onPosClickListener = OnClickListener { }
    private var onNegClickListener = OnClickListener { }
    fun setOnClickListener(negClickListener: OnClickListener, posClickListener: OnClickListener) {
        onNegClickListener = negClickListener
        onPosClickListener = posClickListener
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.ProgressDialogTheme)

        val inflater = requireActivity().layoutInflater
        val layoutResourseId =
            if (rating == -1) R.layout.dialog_layout else R.layout.win_dialog_layout

        val dialogView = inflater.inflate(layoutResourseId, null)

        if (rating != -1) {
            val star1: ImageView = dialogView.findViewById(R.id.star1)
            val star2: ImageView = dialogView.findViewById(R.id.star2)
            val star3: ImageView = dialogView.findViewById(R.id.star3)

            val starFillID = R.mipmap.star_fill
            val starHalfID = R.mipmap.star_half
            setStars(star1, star2, star3, starHalfID, starFillID)
        }

        val exitButton: ImageButton = dialogView.findViewById(R.id.exitImgButton)
        val description: TextView = dialogView.findViewById(R.id.desctiptionTextView)
        val posButton: ImageButton = dialogView.findViewById(R.id.positiveButton)
        val negButton: ImageButton = dialogView.findViewById(R.id.negativeButton)
        val dismissDialogButton: ImageButton = dialogView.findViewById(R.id.dismissDialogButton)


        exitButton.setOnClickListener {
            val intent = Intent(context, LevelSelectionActivity::class.java)
            startActivity(intent)
        }

        dismissDialogButton.setOnClickListener {
            dismiss()
        }

        posButton.apply {
            background = posDrawable
            setOnClickListener {
                onPosClickListener.onClick()
                dismiss()
            }
        }

        negButton.apply {
            background = negDrawable
            setOnClickListener {
                onNegClickListener.onClick()
                dismiss()
            }
        }
        description.text = arguments?.getString("text")
        exitButton.visibility =
            if (arguments?.getBoolean("hideExitButton") == true) View.INVISIBLE else View.VISIBLE


        if (rating == -1)
            dismissDialogButton.visibility = View.GONE


        builder.setView(dialogView)
        return builder.create()
    }


    private fun setStars(
        star1: ImageView,
        star2: ImageView,
        star3: ImageView,
        starHalfID: Int,
        starFillID: Int
    ) {
        when (rating) {
            1 -> star1.setImageResource(starHalfID)

            2 -> star1.setImageResource(starFillID)

            3 -> {
                star1.setImageResource(starFillID)
                star2.setImageResource(starHalfID)
            }

            4 -> {
                star1.setImageResource(starFillID)
                star2.setImageResource(starFillID)
            }

            5 -> {
                star1.setImageResource(starFillID)
                star2.setImageResource(starFillID)
                star3.setImageResource(starHalfID)
            }

            6 -> {
                star1.setImageResource(starFillID)
                star2.setImageResource(starFillID)
                star3.setImageResource(starFillID)
            }
        }
    }
}