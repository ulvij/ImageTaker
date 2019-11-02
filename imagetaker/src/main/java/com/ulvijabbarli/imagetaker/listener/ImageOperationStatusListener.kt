package com.ulvijabbarli.imagetaker.listener

import android.graphics.Bitmap
import java.io.File

abstract class ImageOperationStatusListener {

    private lateinit var file: File

    abstract fun onOperationSuccess(image: Bitmap)

    abstract fun onOperationFailure(errorMessage: String)

    fun setImageFile(file: File) {
        this.file = file
    }

    fun getImageFile(): File {
        return file
    }
}