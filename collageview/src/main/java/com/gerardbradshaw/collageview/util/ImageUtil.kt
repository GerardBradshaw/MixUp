package com.gerardbradshaw.collageview.util

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ImageUtil {

  private const val TAG = "ImageUtil"



  // -------------------- SHARING --------------------

  /**
   * Saves [view] to internal storage and sends the file [Uri] to the listener on completion.
   */
  fun prepareViewForSharing(context: Context, view: View, listener: ImageSavedListener) {
    CoroutineScope(Default).launch {
      val bitmap = createBitmapFrom(view)
      saveBitmapAndNotifyListener(context, bitmap, listener)
    }
  }

  private suspend fun saveBitmapAndNotifyListener(context: Context, bitmap: Bitmap, listener: ImageSavedListener) {
    withContext(IO) {
      val uri = saveBitmapToInternalStorage(context, bitmap)
      notifyListenerBitmapSaveAttempted(listener, uri)
    }
  }

  private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
    try {
      val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg")
      saveImageToStream(bitmap, FileOutputStream(file))
      return FileProvider.getUriForFile(context, context.packageName + ".provider", file)

    } catch (e: IOException) {
      Log.d(TAG, "Error saving image to internal storage.")
    }

    return null
  }

  private suspend fun notifyListenerBitmapSaveAttempted(listener: ImageSavedListener, uri: Uri?) {
    withContext(Main) {
      listener.onReadyToShareImage(uri)
    }
  }



  // -------------------- SAVING --------------------

  /**
   * Saves [view] to internal storage and notifies the listener on completion.
   */
  fun saveViewToGallery(activity: Activity, view: View, listener: ImageSavedListener) {
    requestStoragePermission(activity)

    if (!isStoragePermissionGranted(activity, listener)) return

    CoroutineScope(Default).launch {
      val bitmap = createBitmapFrom(view)
      saveBitmapToGalleryAndNotifyListener(activity, bitmap, listener)
    }
  }

  private suspend fun saveBitmapToGalleryAndNotifyListener(context: Context, bitmap: Bitmap, listener: ImageSavedListener) {
    withContext(IO) {
      val isSavedSuccessfully = saveBitmapToGallery(context, bitmap)
      notifyListenerOfImageSavedToGalleryResult(listener, isSavedSuccessfully)
    }
  }

  private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    val folderName = "MixUp"

    return if (android.os.Build.VERSION.SDK_INT >= 29) {
      saveBitmapToGalleryAndroidQ(context, bitmap, getFilename(), folderName)
    }
    else {
      saveBitmapToGalleryAndroidM(context, bitmap, getFilename(), folderName)
    }
  }

  @RequiresApi(29)
  private fun saveBitmapToGalleryAndroidQ(context: Context, bitmap: Bitmap, filename: String, folderName: String): Boolean {
    val contentValues = getContentValues(filename).apply {
      put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/$folderName")
      put(MediaStore.Images.Media.IS_PENDING, true)
      put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    if (uri != null) {
      val isSavedSuccessfully = saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))

      contentValues.put(MediaStore.Images.Media.IS_PENDING, false)
      context.contentResolver.update(uri, contentValues, null, null)

      return isSavedSuccessfully
    }

    return false
  }

  @Suppress("DEPRECATION")
  @TargetApi(21)
  private fun saveBitmapToGalleryAndroidM(context: Context, bitmap: Bitmap, filename: String, folderName: String): Boolean {
    val directory = File(Environment.getExternalStorageDirectory().toString() + "/" + folderName)

    if (!directory.exists()) directory.mkdirs()

    val file = File(directory, filename)
    val isSavedSuccessfully = saveImageToStream(bitmap, FileOutputStream(file))

    val values = getContentValues(filename)
    values.put(MediaStore.Images.Media.DATA, file.absolutePath)
    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    return isSavedSuccessfully
  }

  private suspend fun notifyListenerOfImageSavedToGalleryResult(listener: ImageSavedListener, isSavedSuccessfully: Boolean) {
    withContext(Main) {
      listener.onCollageSavedToGallery(isSavedSuccessfully)
    }
  }



  // -------------------- UTIL --------------------

  private fun createBitmapFrom(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
  }

  @SuppressLint("SimpleDateFormat") // not relevant - format for filename purpose only
  private fun getFilename(): String {
    val sdf = SimpleDateFormat("yyyyMMddHHmmss")
    val dateAndTime = sdf.format(Calendar.getInstance().time)

    return "mixup$dateAndTime"
  }

  private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?): Boolean {
    try {
      outputStream?.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        it.flush()
        it.close()
        return true
      }

    } catch (e: FileNotFoundException) {
      Log.d(TAG, "Save to gallery failed due to FileNotFoundException.")
      e.printStackTrace()

    } catch (e: IOException) {
      Log.d(TAG, "Save to gallery failed due to IOException.")
      e.printStackTrace()
    }

    return false
  }

  private fun getContentValues(filename: String): ContentValues {
    return ContentValues().apply {
      put(MediaStore.Images.Media.MIME_TYPE, "image/png")
      put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
      put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
    }
  }



  // -------------------- PERMISSION CHECKS --------------------

  private fun requestStoragePermission(activity: Activity) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

      val shouldRequestPermission =
        activity.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED

      if (shouldRequestPermission) {
        if (activity.shouldShowRequestPermissionRationale(requiredPermission)) {
          Toast.makeText(
            activity,
            "Storage permission is needed to save to gallery.",
            Toast.LENGTH_LONG).show()
        }
        val storagePermissions = arrayOf(requiredPermission)
        activity.requestPermissions(storagePermissions, 1)
      }
    }
  }

  private fun isStoragePermissionGranted(activity: Activity, listener: ImageSavedListener): Boolean {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

      if (activity.checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
        Log.d(TAG, "Save to gallery failed. Permission denied.")
        listener.onCollageSavedToGallery(false)
        return false
      }
    }
    return true
  }


  // -------------------- INTERFACE --------------------

  interface ImageSavedListener {
    fun onCollageSavedToGallery(isSavedSuccessfully: Boolean)
    fun onReadyToShareImage(uri: Uri?)
  }
}