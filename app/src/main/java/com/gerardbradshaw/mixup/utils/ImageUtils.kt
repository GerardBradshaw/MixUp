package com.gerardbradshaw.mixup.utils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageUtils(private val activity: Activity, private val listener: ImageSavedListener) {

  private fun createBitmapFrom(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
  }

  private fun getFilename(): String {
    val sdf = SimpleDateFormat("yyyyMMddHHmmss")
    val dateAndTime = sdf.format(Calendar.getInstance().time)

    return "mixup$dateAndTime.jpg"
  }

  /**
   * Saves [view] to internal storage and sends the file [Uri] to the listener on completion.
   */
  fun prepareViewForSharing(view: View) {
    CoroutineScope(Default).launch {
      val bitmap = createBitmapFrom(view)
      saveBitmapAndNotifyListener(bitmap)
    }
  }

  private suspend fun saveBitmapAndNotifyListener(bitmap: Bitmap) {
    withContext(IO) {
      val uri = saveBitmapToInternalStorage(bitmap)
      notifyListenerImageReadyToShare(uri)
    }
  }

  private fun saveBitmapToInternalStorage(bitmap: Bitmap): Uri? {
    var uri: Uri? = null

    try {
      val file = File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg")
      val outStream = FileOutputStream(file)
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
      outStream.close()
      uri = FileProvider.getUriForFile(
        activity,
        activity.packageName + ".provider",
        file)

    } catch (e: IOException) {
      Log.d(TAG, "Error saving image to internal storage.")
    }
    return uri
  }

  private suspend fun notifyListenerImageReadyToShare(uri: Uri?) {
    withContext(Main) {
      listener.onReadyToShareImage(uri)
    }
  }

  /**
   * Saves [view] to internal storage and notifies the listener on completion.
   */
  fun saveViewToGallery(view: View) {
    checkPermissions()

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

      if (activity.checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
        Log.d(TAG, "Save to gallery failed due to user permission denial")
        listener.onImageSavedToGallery(false)
        return
      }
    }

    CoroutineScope(Default).launch {
      val bitmap = createBitmapFrom(view)
      saveBitmapToGalleryAndNotifyListener(bitmap)
    }
  }

  private fun checkPermissions() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

      val shouldRequestPermission =
        activity.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED

      if (shouldRequestPermission) {
        if (activity.shouldShowRequestPermissionRationale(requiredPermission)) {
          Toast.makeText(activity,
            "Storage permission is needed to save to gallery", Toast.LENGTH_SHORT).show()
        }
        val storagePermissions = arrayOf(requiredPermission)
        activity.requestPermissions(storagePermissions, 1)
      }
    }
  }

  private suspend fun saveBitmapToGalleryAndNotifyListener(bitmap: Bitmap) {
    withContext(IO) {
      val isSuccess = saveBitmapToGallery(bitmap)
      notifyListenerImageSavedToGallery(isSuccess)
    }
  }

  private fun saveBitmapToGallery(bitmap: Bitmap): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
      saveBitmapToGalleryAndroidQ(bitmap, getFilename())
      true
    } else saveBitmapToGalleryAndroidM(bitmap, getFilename())
  }

  private fun saveBitmapToGalleryAndroidQ(bitmap: Bitmap, filename: String) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/MixUp")
      }

      val resolver = activity.contentResolver
      val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
      val outStream = resolver.openOutputStream(uri!!)

      if (outStream != null) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
      }
    }
  }

  @Suppress("DEPRECATION")
  private fun saveBitmapToGalleryAndroidM(bitmap: Bitmap, filename: String): Boolean {
    try {
      val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
      val imageFile = File(imagesDir, "$filename.jpeg")

      val outStream = FileOutputStream(imageFile)
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
      outStream.flush()
      outStream.close()
      return true
    }
    catch (e: FileNotFoundException) {
      Log.d(TAG, "Save to gallery failed due to FileNotFoundException.")
      e.printStackTrace()
      return false
    }
    catch (e: IOException) {
      Log.d(TAG, "Save to gallery failed due to IOException.")
      e.printStackTrace()
      return false
    }
  }

  private suspend fun notifyListenerImageSavedToGallery(isSuccess: Boolean) {
    withContext(Main) {
      listener.onImageSavedToGallery(isSuccess)
    }
  }

  interface ImageSavedListener {
    fun onImageSavedToGallery(isSuccess: Boolean)
    fun onReadyToShareImage(uri: Uri?)
  }

  companion object {
    private const val TAG = "ImageUtils"
  }
}