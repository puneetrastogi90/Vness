package com.pr.imagemacro.interfaces

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri

/**
 * This interface is used as a wrapper over the photo library that we plan to use.
 * By this, we are following the Dependency Inversion Principle of SOLID Principles
 *  If in future, we plan to use a new library, we need to implement this interface and
 *  override the funtions, thereby requiring changes in only once class.
 */
interface MyImageEditor<T> {
    fun setEditorView(view: T)
    fun changeBackgroundColor(colorId: Int)
    fun addText(inputText: String?, colorCode: Int)
    fun addImage(uri: Uri?, contentResolver: ContentResolver)
    fun undoAction()
    fun saveFile(filePath: String, format: String, fileSaveListener: ImageSaveListener)
}