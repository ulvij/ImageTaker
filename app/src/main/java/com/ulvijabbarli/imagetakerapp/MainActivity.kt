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

        imageInstance = ImageTaker
            .getInstance(this)
            .setActivity(this)
            .setOperationListener(
                object : ImageOperationStatusListener() {
                    override fun onOperationSuccess(image: Bitmap) {
                        photo.setImageBitmap(image)
                        text_path.setText(getImageFile().toString()) // you can get image as File by calling getImageFile method
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
