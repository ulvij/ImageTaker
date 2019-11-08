package com.ulvijabbarli.imagetakerapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ulvijabbarli.imagetaker.ImageTaker
import com.ulvijabbarli.imagetaker.listener.ImageOperationStatusListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var imageInstance: ImageTaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageInstance = ImageTaker.getInstance(this)        // getting instance of image taker here
            .setActivity(this)              // setting current activity here
            .enableCrop()                   // enable crop here (default status is disabled)
            .setOperationListener(
                object : ImageOperationStatusListener() {
                    override fun onOperationSuccess(image: Bitmap) {
                        photo.setImageBitmap(image)             // get image as bitmap here
                        text_path.text =
                            getImageFile().toString()           // you can get image as file by calling getImageFile method
                    }

                    override fun onOperationFailure(errorMessage: String) {
                        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                    }
                })

        button_camera.setOnClickListener {
            imageInstance.openCamera()
        }

        button_gallery.setOnClickListener {
            imageInstance.openGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageInstance.onActivityResult(requestCode, resultCode, data)
    }
}
