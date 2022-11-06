package com.pr.imagemacro

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File


val colors = arrayListOf<Int>(
    R.color.purple_200,
    R.color.yellow,
    R.color.gray,
    R.color.black,
    R.color.aqua,
    R.color.teal_200,
    R.color.pink,
    R.color.red,
    R.color.brown,
    R.color.white
)

const val FILE_PROVIDER_AUTHORITY = "com.pr.imagemacro.fileprovider"

internal fun getRandomColor(index: Int): Int {
    return colors.get(index % 10)
}

internal fun isSdkHigherThan28(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}

internal fun buildFileProviderUri(context: Context, uri: Uri): Uri {
    if (isSdkHigherThan28()) {
        return uri
    }
    val path: String = uri.path ?: throw IllegalArgumentException("URI Path Expected")

    return FileProvider.getUriForFile(
        context,
        FILE_PROVIDER_AUTHORITY,
        File(path)
    )
}
