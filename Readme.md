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
   	        implementation 'com.github.Ulvi583:ImageTaker:version'
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
        .setOperationListener(                       // setting result listener to library and receive result according to state
            object : ImageOperationStatusListener {
                override fun onOperationSuccess(image: Bitmap) {
                    // we can get the image here as a bitmap
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

## Authors

- Ulvi Jabbarli - Android Developer




