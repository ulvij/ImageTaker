package com.ulvijabbarli.imagetaker.listener

import android.graphics.Bitmap

interface ImageOperationStatusListener{

    fun onOperationSuccess(image:Bitmap)

    fun onOperationFailure(errorMessage:String)

}