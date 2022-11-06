package com.pr.imagemacro.viewmodels

import androidx.lifecycle.ViewModel

class MainViewmodel : ViewModel() {
    var index = 0

    fun getRandomColor() = com.pr.imagemacro.getRandomColor(index++)
}