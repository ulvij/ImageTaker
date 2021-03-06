# Image Taker

This is a simple Image taker library that makes it easy to take an image from a camera or gallery by getting the responsibility of different situations that are came from the variety of the android versions.

## Getting Started

### Installing

**Step 1.** Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```
dependencies {
   	        implementation 'com.github.ulvij:ImageTaker:v1.0.0'
   	}
```

### Configuration

**Step 1.** Add required permissions to manifest

```
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/> 
```

**Step 2.** We need to create fileProvider to android know where he can save the picture file, go to your Manifest and add this code:

```
<provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="package_name.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths"/>
        </provider>
```

**Note** : Remember where is package_name, you need to change with your app package.

Now you need to create the resource telling the path to save the picture file, in the code above we use the name _file_paths_ but you can change to one name as you wish, but remember to change in the provide you have create at Manifest:

```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path
        name="my_images"
        path="Android/data/package_name/files/Pictures" />
</paths>
```
**Note** : Again you need to change package_name with the package of your app.

Ok now we can start to code, yhayy

### Usage

For using, we have to create the instance of ImageTaker and make some little configuration.

```kotlin
private var imageInstance = ImageTaker
        .getInstance(this)                           // setting context of current state
        .setActivity(this)                           // setting activity
	.enableCrop()                   	     // making enable the crop feature, which is disabled by default
        .setOperationListener(                       // setting result listener to library and receive result according to state
            object : ImageOperationStatusListener {
                override fun onOperationSuccess(image: Bitmap) {
                    // we can get the image here as a bitmap

		    // we can get the image as a file by calling getImageFile() method here	
                }

                override fun onOperationFailure(errorMessage: String) {
                    // we can get the error message here
                }

            })
```
After creating the instance of ImageTaker, we have to forward the params of ActivityResult to Image Taker

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageInstance.onActivityResult(requestCode, resultCode, data)       // here we are forwarding the params to ImageTaker
    }
```

After this, we can call the camera and gallery everywhere like this

```kotlin
     // for Camera
     imageInstance.openCamera() 

     // for Gallery   
     imageInstance.openGallery() 
```

## Used Libraries

- __[Android-Image-Cropper](https://github.com/ArthurHub/Android-Image-Cropper)__

## Authors

- __Ulvi Jabbarli__ - Android Developer

## License
```
MIT License

Copyright (c) 2019 Ulvi Jabbarli

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
