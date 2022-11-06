package com.pr.imagemacro.interfaces

interface ImageSaveListener {
    fun onFileSaveSuccess(imagePath: String)
    fun onFileSaveFailed(exception: Exception)
}