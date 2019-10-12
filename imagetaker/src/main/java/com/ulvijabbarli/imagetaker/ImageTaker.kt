package com.ulvijabbarli.imagetaker

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.ulvijabbarli.imagetaker.listener.ImageOperationStatusListener
import com.ulvijabbarli.imagetaker.permission.AppPermissionsRunTime
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ImageUtil private constructor(private var context: Context) {

    class StatusCode {
        companion object {
            const val FROM_CAMERA_FOR_OVER_VERSION_N_REQUEST_ID: Int = 100
            const val FROM_CAMERA_FOR_UNDER_VERSION_N_REQUEST_ID: Int = 200
            const val FROM_CAMERA_FOR_GALLERY: Int = 300
        }
    }

    private lateinit var imagePath: String
    private lateinit var imageUri: Uri
    private lateinit var activity: Activity
    private lateinit var operationStatusListener: ImageOperationStatusListener
    private var permissionList: ArrayList<AppPermissionsRunTime.Permission> = ArrayList()

    init {
        permissionList.add(AppPermissionsRunTime.Permission.CAMERA)
        permissionList.add(AppPermissionsRunTime.Permission.WRITE_EXTERNAL_STORAGE)
        permissionList.add(AppPermissionsRunTime.Permission.READ_EXTERNAL_STORAGE)
    }

    companion object : SingletonHolder<ImageUtil, Context>(::ImageUtil)

    fun setActivity(activity: Activity): ImageUtil {
        this.activity = activity
        return this
    }

    fun setOperationListener(operationStatusListener: ImageOperationStatusListener): ImageUtil {
        this.operationStatusListener = operationStatusListener
        return this
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                StatusCode.FROM_CAMERA_FOR_OVER_VERSION_N_REQUEST_ID -> getImageFromCameraByImagePath(imagePath)
                StatusCode.FROM_CAMERA_FOR_UNDER_VERSION_N_REQUEST_ID -> getImageFromCameraByImageUri(imageUri)
                StatusCode.FROM_CAMERA_FOR_GALLERY -> getImageFromGalleryByIntentData(data)
            }
        }
    }

    fun openGallery() {
        if (AppPermissionsRunTime.getInstance().getPermission(permissionList, activity)) {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activity.startActivityForResult(pickPhoto, StatusCode.FROM_CAMERA_FOR_GALLERY)
        }
    }

    fun openCamera() {
        if (AppPermissionsRunTime.getInstance().getPermission(permissionList, activity)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                createIntentForCameraWhenVersionIsUnderN()
            } else {
                createIntentForCameraWhenVersionIsEqualAndOverN()
            }
        }
    }

    private fun createIntentForCameraWhenVersionIsEqualAndOverN() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                operationStatusListener.onOperationFailure(ex.message.toString())
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",
                    photoFile
                )

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                activity.startActivityForResult(
                    takePictureIntent,
                    StatusCode.FROM_CAMERA_FOR_OVER_VERSION_N_REQUEST_ID
                )

            }
        } else {
            operationStatusListener.onOperationFailure(context.resources.getString(R.string.error_when_getting_image_from_camera))
        }
    }

    private fun createIntentForCameraWhenVersionIsUnderN() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                operationStatusListener.onOperationFailure(ex.message.toString())
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageUri = Uri.fromFile(photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                activity.startActivityForResult(
                    takePictureIntent,
                    StatusCode.FROM_CAMERA_FOR_UNDER_VERSION_N_REQUEST_ID
                )
            }
        }
    }

    private fun getImageFromCameraByImageUri(imageUri: Uri?) {
        var lastImage: Bitmap? = null
        if (imageUri != null) {
            try {
                lastImage = handleSamplingAndRotationBitmap(imageUri)
            } catch (e: IOException) {
                val unScaledImage = uriToBitmap(imageUri)
                if (unScaledImage != null)
                    lastImage = Bitmap.createScaledBitmap(unScaledImage, 1000, 1000, true)
            }

            if (lastImage != null) {
                val imageFile = convertBitmapToFile(lastImage)
                try {
                    val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath)
                    operationStatusListener.onOperationSuccess(bitmap)
                } catch (e: Exception) {
                    operationStatusListener.onOperationFailure(e.message.toString())
                }
            }

        }
    }

    private fun getImageFromGalleryByIntentData(data: Intent?) {
        val imageFile: File?
        val imageUri = data?.data
        if (imageUri == null) {
            operationStatusListener.onOperationFailure(context.resources.getString(R.string.error_when_getting_image_from_camera))
            return
        }

        imageFile = if (isNewGooglePhotosUri(imageUri)) {
            getPhotoFile(imageUri)
        } else {
            getRealPathFromURI(imageUri)
        }

        try {
            val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath)
            operationStatusListener.onOperationSuccess(bitmap)
        } catch (e: Exception) {
            operationStatusListener.onOperationFailure(e.message.toString())
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
        imagePath = image.absolutePath
        return image
    }

    private fun getImageFromCameraByImagePath(imagePath: String) {
        try {
            val imageFile = File(imagePath)
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            if (bitmap != null) {
                operationStatusListener.onOperationSuccess(bitmap)
            } else {
                operationStatusListener.onOperationFailure(context.getString(R.string.error_image_is_null))
            }
        } catch (e: Exception) {
            operationStatusListener.onOperationFailure(e.message.toString())
        }

    }


    //4 Functions below is for checking rotate degree and rotate if required
    @Throws(IOException::class)
    private fun handleSamplingAndRotationBitmap(selectedImage: Uri): Bitmap? {
        val maxHeight = 1024
        val maxWidth = 1024

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var imageStream = context.contentResolver.openInputStream(selectedImage)
        BitmapFactory.decodeStream(imageStream, null, options)
        imageStream!!.close()

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        imageStream = context.contentResolver.openInputStream(selectedImage)
        var img = BitmapFactory.decodeStream(imageStream, null, options)

        img = rotateImageIfRequired(img, selectedImage)
        return img
    }

    /**
     * Calculate an inSampleSize for use in a [BitmapFactory.Options] object when decoding
     * bitmaps using the decode* methods from [BitmapFactory]. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     * method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            val totalPixels = (width * height).toFloat()

            // Anything more than 2x the requested pixels we'll sample down further
            val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
        }
        return inSampleSize
    }

    /**
     * Rotate an image if required.
     *
     * @param bitmapImage   The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    @Throws(IOException::class)
    private fun rotateImageIfRequired(bitmapImage: Bitmap?, selectedImage: Uri): Bitmap? {
        var img = bitmapImage
        img = Bitmap.createScaledBitmap(img!!, 1000, 1000, true)
        val ei = ExifInterface(selectedImage.path!!)

        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap?, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img!!, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }


    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        var image: Bitmap? = null
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return image
    }

    private fun convertBitmapToFile(bitmap: Bitmap): File? {
        val filesDir = context.filesDir
        val imageFile = File(filesDir, "photo.jpg")
        val os: OutputStream
        return try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
            imageFile
        } catch (e: Exception) {
            null
        }

    }

    private fun getPhotoFile(selectedImageUri: Uri): File? {
        try {
            val `is` = context.contentResolver.openInputStream(selectedImageUri)
            if (`is` != null) {
                val pictureBitmap = BitmapFactory.decodeStream(`is`)
                val bytes = ByteArrayOutputStream()
                pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes)
                val output = File(getCacheDir(), System.currentTimeMillis().toString() + ".jpg")
                output.createNewFile()
                val fo = FileOutputStream(output)
                fo.write(bytes.toByteArray())
                fo.close()
                return output
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun getCacheDir(): File? {
        var cacheDir = context.getExternalFilesDir(null)
        if (cacheDir != null) {
            if (!cacheDir.exists())
                cacheDir.mkdirs()
        } else {
            cacheDir = context.cacheDir
        }
        return cacheDir
    }

    private fun getRealPathFromURI(uri: Uri): File {
        var path = ""
        val dataTypeForColumnIndex = "_data"
        if (context.contentResolver != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val idx = cursor.getColumnIndex(dataTypeForColumnIndex)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return File(path)
    }

    private fun isNewGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.contentprovider" == uri.authority
    }
}

open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
