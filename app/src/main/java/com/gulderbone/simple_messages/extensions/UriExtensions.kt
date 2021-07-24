package com.gulderbone.simple_messages.extensions

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

fun Uri.getFilePathFromContentUri(contentResolver: ContentResolver): String? {
    val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
    val cursor: Cursor = contentResolver.query(this, filePathColumn, null, null, null) ?: return null

    cursor.moveToFirst()

    val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])

    val filePath = cursor.getString(columnIndex)
    cursor.close()

    return filePath
}