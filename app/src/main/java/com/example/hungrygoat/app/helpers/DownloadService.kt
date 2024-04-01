package com.example.hungrygoat.app.helpers

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore

class DownloadService {

    fun saveBMPToAFile(
        context: Context,
        bmp: Bitmap,
        folderName: String,
        fileName: String,
        callback: () -> Unit
    ) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
        }

        val contentResolver = context.contentResolver
        val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val outputUri = contentResolver.insert(request, contentValues)

        outputUri?.let { uri ->
            contentResolver.openOutputStream(uri).use { outputStream ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream ?: return)
                callback()
            }
        }
    }

    fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId: Int = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString()
        else context.getString(stringId)
    }
}