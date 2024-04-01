package com.example.hungrygoat.app.helpers.alertDialogs

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DialogHelper {

    companion object {
        const val DIALOG_TAG = "MyDialog"
    }

    fun createDialog(
        activity: AppCompatActivity,
        bundle: Bundle,
        negDrawable: Drawable?,
        posDrawable: Drawable,
        negClickListener: GameDialog.OnClickListener,
        posClickListener: GameDialog.OnClickListener,
        rating: Int = -1
    ) {
        val dialog = createGameDialog(rating, negDrawable, posDrawable)

        dialog.setOnClickListener(negClickListener, posClickListener)
        dialog.arguments = bundle
        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    fun createLevelConditionDialog(
        activity: AppCompatActivity,
        mipmapID: Int,
        levelCond: String,
        posClickListener: GameDialog.OnClickListener
    ) {
        val dialog = LevelConditionDialog(mipmapID, levelCond, posClickListener).apply {
            isCancelable = false
        }
        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun createGameDialog(
        rating: Int = -1,
        negDrawable: Drawable?,
        posDrawable: Drawable
    ) = GameDialog(rating, negDrawable, posDrawable).apply {
        isCancelable = false
    }
}
