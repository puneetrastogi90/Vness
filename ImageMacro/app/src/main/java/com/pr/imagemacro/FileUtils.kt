package com.pr.imagemacro

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import kotlin.Throws
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class FileUtils(private val contentResolver: ContentResolver) : LifecycleObserver {
    private val executor: ExecutorService? = Executors.newSingleThreadExecutor()
    private val fileCreatedResult: MutableLiveData<FileMeta> = MutableLiveData()
    private var resultListener: OnFileCreated? = null
    private val observer = Observer { fileMeta: FileMeta ->
        if (resultListener != null) {
            resultListener!!.onFileCreated(
                fileMeta.isCreated,
                fileMeta.filePath,
                fileMeta.error,
                fileMeta.uri
            )
        }
    }

    constructor(activity: AppCompatActivity) : this(activity.contentResolver) {
        addObserver(activity)
    }

    private fun addObserver(lifecycleOwner: LifecycleOwner) {
        fileCreatedResult.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        executor?.shutdownNow()
    }


    fun createFile(fileNameToSave: String, listener: OnFileCreated?) {
        resultListener = listener
        executor!!.submit {
            var cursor: Cursor? = null
            try {

                // Build the edited image URI for the MediaStore
                val newImageDetails = ContentValues()
                val imageCollection = buildUriCollection(newImageDetails)
                val editedImageUri =
                    getEditedImageUri(fileNameToSave, newImageDetails, imageCollection)

                // Query the MediaStore for the image file path from the image Uri
                cursor = contentResolver.query(
                    editedImageUri,
                    arrayOf(MediaStore.Images.Media.DATA),
                    null,
                    null,
                    null
                )
                val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                val filePath = cursor.getString(columnIndex)

                updateResult(true, filePath, null, editedImageUri, newImageDetails)
            } catch (ex: Exception) {
                ex.printStackTrace()
                updateResult(false, null, ex.message, null, null)
            } finally {
                cursor?.close()
            }
        }
    }

    @Throws(IOException::class)
    private fun getEditedImageUri(
        fileNameToSave: String,
        newImageDetails: ContentValues,
        imageCollection: Uri
    ): Uri {
        newImageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, fileNameToSave)
        val editedImageUri = contentResolver.insert(imageCollection, newImageDetails)
        val outputStream = contentResolver.openOutputStream(editedImageUri!!)
        outputStream!!.close()
        return editedImageUri
    }

    @SuppressLint("InlinedApi")
    private fun buildUriCollection(newImageDetails: ContentValues): Uri {
        val imageCollection: Uri
        if (isSdkHigherThan28()) {
            imageCollection = MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
            newImageDetails.put(MediaStore.Images.Media.IS_PENDING, 1)
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        return imageCollection
    }

    @SuppressLint("InlinedApi")
    fun notifyThatFileIsNowPubliclyAvailable(contentResolver: ContentResolver) {
        if (isSdkHigherThan28()) {
            executor?.submit {
                val value = fileCreatedResult.value
                if (value != null) {
                    value.imageDetails?.clear()
                    value.imageDetails?.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(value.uri!!, value.imageDetails, null, null)
                }
            }
        }
    }

    private class FileMeta(
        var isCreated: Boolean, var filePath: String?,
        var uri: Uri?, var error: String?,
        var imageDetails: ContentValues?
    )

    interface OnFileCreated {
        fun onFileCreated(created: Boolean, filePath: String?, error: String?, uri: Uri?)
    }

    private fun updateResult(
        result: Boolean,
        filePath: String?,
        error: String?,
        uri: Uri?,
        newImageDetails: ContentValues?
    ) {
        fileCreatedResult.postValue(FileMeta(result, filePath, uri, error, newImageDetails))
    }

}