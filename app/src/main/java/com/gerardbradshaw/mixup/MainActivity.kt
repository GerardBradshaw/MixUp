package com.gerardbradshaw.mixup

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gerardbradshaw.mixup.ui.moreapps.MoreAppsFragment
import com.google.android.material.navigation.NavigationView
import java.io.*

class MainActivity : AppCompatActivity(), MoreAppsFragment.OnFragmentCreatedListener {

  private lateinit var appBarConfiguration: AppBarConfiguration
  private var menu: Menu? = null
  private val LOG_TAG = MainActivity::class.java.name

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
      R.id.action_share -> {
        shareImage()
      }
      R.id.action_reset -> {
        showToast("reset not implemented")
      }
      R.id.action_settings -> {
        showToast("settings not implemented")
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun shareImage() {
    val imageViewGroup = findViewById<FrameLayout>(R.id.image_container)

    if (imageViewGroup != null) {
      val bitmap = getBitmapFromView(imageViewGroup)
      if (bitmap != null) {
        saveBitmapToSdCard(bitmap)
        return
      }
    }
    Log.d(LOG_TAG, "Share failed. Unable to located image container.")
    showToast("An error occurred")
  }

  private fun saveBitmapToSdCard(bitmap: Bitmap) {
    val filename = "MixUp"

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
        showToast("My API is 23-25")
      }
      if (outStream != null) {
        showToast("saving...")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
        showToast("saved successfully")
        return
      }
    }
    catch (e: FileNotFoundException) { e.printStackTrace() }
    catch (e: IOException) { e.printStackTrace() }

    Log.d(LOG_TAG, "Error saving image")
    showToast("Error saving image")
  }

  private fun getBitmapFromView(view: View): Bitmap? {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
  }

  private fun showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }
}