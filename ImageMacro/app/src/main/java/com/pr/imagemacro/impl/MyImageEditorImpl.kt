package com.pr.imagemacro.impl

import android.Manifest
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import com.pr.imagemacro.interfaces.ImageSaveListener
import com.pr.imagemacro.interfaces.MyImageEditor
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import java.io.IOException

/**
 *This class is the implementation class of MyImageEditor, which uses the library project 'photoeditor'
 * functions.
 */
class MyImageEditorImpl : MyImageEditor<PhotoEditorView> {

    private var _editor: PhotoEditor? = null
    private lateinit var _photoEditorView: PhotoEditorView

    override fun setEditorView(view: PhotoEditorView) {
        _photoEditorView = view
        _editor = view?.run {
            PhotoEditor.Builder(this.context, this)
                .setPinchTextScalable(true)
                .build()
        }
    }

    override fun changeBackgroundColor(colorId: Int) {
        _photoEditorView?.source?.setImageResource(colorId)
    }

    override fun addText(inputText: String?, colorCode: Int) {
        val styleBuilder = TextStyleBuilder()
        styleBuilder.withTextColor(colorCode)
        _editor?.addText(inputText, styleBuilder)
    }

    override fun addImage(uri: Uri?, contentResolver: ContentResolver) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(
                contentResolver, uri
            )
            _editor?.addImage(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun undoAction() {
        _editor?.undo()
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    override fun saveFile(filePath: String, formatString: String, fileSaveListener: ImageSaveListener) {
        val format = if(formatString.lowercase().equals("jpeg")){
            Bitmap.CompressFormat.JPEG
        }else{
            Bitmap.CompressFormat.PNG
        }
        val settings = SaveSettings.Builder().setCompressFormat(format)
        settings.isClearViewsEnabled = false
        settings.isTransparencyEnabled = false

        _editor?.saveAsFile(
            filePath,
            settings.build(),
            object : PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    fileSaveListener.onFileSaveSuccess(imagePath)
                }

                override fun onFailure(exception: Exception) {
                    fileSaveListener.onFileSaveFailed(exception)
                }
            })
    }
}