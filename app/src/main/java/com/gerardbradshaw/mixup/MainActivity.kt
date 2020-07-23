package com.gerardbradshaw.mixup

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gerardbradshaw.mixup.ui.moreapps.MoreAppsFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val LOG_TAG = "MainActivity"

class MainActivity : AppCompatActivity(), MoreAppsFragment.OnFragmentCreatedListener {

  private lateinit var appBarConfiguration: AppBarConfiguration
  private var menu: Menu? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    initUi()
  }

  private fun initUi() {
    val toolbar: Toolbar = findViewById(R.id.editor_toolbar)
    setSupportActionBar(toolbar)

    val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
    val navigationView: NavigationView = findViewById(R.id.nav_view)
    val navigationController = findNavController(R.id.nav_host_fragment)

    appBarConfiguration = AppBarConfiguration(
      setOf(R.id.nav_mix_up, R.id.nav_more_apps), drawerLayout)
    setupActionBarWithNavController(navigationController, appBarConfiguration)
    navigationView.setupWithNavController(navigationController)
  }

  override fun onFragmentChanged(isOptionsMenuVisible: Boolean) {
    toggleOptionsMenuVisibility(isOptionsMenuVisible)
  }

  private fun toggleOptionsMenuVisibility(isOptionsMenuVisible: Boolean) {
    menu?.setGroupVisible(R.id.main_options_menu_group, isOptionsMenuVisible)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    this.menu = menu
    menuInflater.inflate(R.menu.main_options_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_share -> shareImage()
      R.id.action_save -> saveImageToGallery()
      R.id.action_reset -> resetFrame()
      R.id.action_settings -> openSettings()
    }
    return super.onOptionsItemSelected(item)
  }

  private fun shareImage() {
    val spinnerContainer = findViewById<FrameLayout>(R.id.progress_bar_frame)
    spinnerContainer.visibility = View.VISIBLE

    val imageViewGroup = findViewById<FrameLayout>(R.id.image_container)

    if (imageViewGroup != null) captureViewAndShare(imageViewGroup)
    else {
      Log.d(LOG_TAG, "Share failed. Unable to located image container.")
      Toast.makeText(this, "An error occurred :(", Toast.LENGTH_LONG).show()
      spinnerContainer.visibility = View.GONE
    }
  }

  private fun captureViewAndShare(view: View) {
    CoroutineScope(Default).launch {
      val bitmap = captureViewToBitmap(view)
      saveBitmapToInternalStorageAndOpenShareSheet(bitmap)
    }
  }

  private fun captureViewToBitmap(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
  }

  private suspend fun saveBitmapToInternalStorageAndOpenShareSheet(bitmap: Bitmap) {
    withContext(IO) {
      var uri: Uri? = null

      try {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "mixup.jpg")
        val outStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.close()
        uri = FileProvider.getUriForFile(
          applicationContext,
          applicationContext.packageName + ".provider",
          file)

      } catch (e: IOException) {
        Log.d(LOG_TAG, "Error saving image to internal storage.")
      }
      startShareSheet(uri)
    }
  }

  private suspend fun startShareSheet(uri: Uri?) {
    withContext(Main) {
      findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.GONE

      if (uri == null) {
        Log.d(LOG_TAG, "Share failed. Uri was null.")
        Toast.makeText(applicationContext, "An error occurred :(", Toast.LENGTH_SHORT).show()

      } else {
        val shareIntent: Intent = Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          type = "image/jpeg"
        }
        startActivity(Intent.createChooser(shareIntent, "Share to..."))
      }
    }
  }

  private fun openShareSheet(uri: Uri?) {

  }

  private fun saveImageToGallery() {
    val spinnerContainer = findViewById<FrameLayout>(R.id.progress_bar_frame)
    spinnerContainer.visibility = View.VISIBLE

    val imageViewGroup = findViewById<FrameLayout>(R.id.image_container)

    if (imageViewGroup != null) captureViewAndSave(imageViewGroup)
    else {
      Log.d(LOG_TAG, "Save failed. Unable to located image container.")
      Toast.makeText(this, "An error occurred :(", Toast.LENGTH_LONG).show()
      spinnerContainer.visibility = View.GONE
    }
  }

  private fun captureViewAndSave(view: View) {
    CoroutineScope(IO).launch {
      val bitmap = captureViewToBitmap(view)
      saveBitmapToGallery(bitmap)
      withContext(Main) {
        findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.GONE
      }
    }
  }

  private fun resetFrame() {
    val navController = findNavController(R.id.nav_host_fragment)
    navController.navigate(R.id.nav_mix_up)
  }

  private fun openSettings() {
    Toast.makeText(applicationContext, "settings not implemented", Toast.LENGTH_SHORT).show()
  }

  private fun getDateAndTimeString(): String {
    val format = SimpleDateFormat("yyyyMMddHHmmss")
    return format.format(Calendar.getInstance().time)
  }

  private fun saveBitmapToGallery(bitmap: Bitmap) {
    val dateAndTime = getDateAndTimeString()
    val filename = "mixup$dateAndTime"

    try {
      val outStream: OutputStream?

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
          put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
          put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/MixUp")
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        outStream = resolver.openOutputStream(uri!!)

      } else {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
          val permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
          if (permission != PackageManager.PERMISSION_GRANTED) {
            val storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(storagePermissions, 1)
          }
        }
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val imageFile = File(imagesDir, "$filename.jpeg")
        outStream = FileOutputStream(imageFile)
      }
      if (outStream != null) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
        return
      }
    }
    catch (e: FileNotFoundException) { e.printStackTrace() }
    catch (e: IOException) { e.printStackTrace() }

    Log.d(LOG_TAG, "Error saving image")
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }
}