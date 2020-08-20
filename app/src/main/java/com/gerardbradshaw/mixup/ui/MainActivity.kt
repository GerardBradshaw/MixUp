package com.gerardbradshaw.mixup.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.gerardbradshaw.mixup.BaseApplication
import com.gerardbradshaw.mixup.utils.ImageUtils
import com.gerardbradshaw.mixup.R
import com.google.android.material.navigation.NavigationView

private const val LOG_TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ImageUtils.ImageSavedListener,
  NavController.OnDestinationChangedListener {

  private lateinit var appBarConfig: AppBarConfiguration
  private var menu: Menu? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    (application as BaseApplication).getAppComponent()
      .activityComponent().create().inject(this)

    initUi()
  }

  private fun initUi() {
    val toolbar: Toolbar = findViewById(R.id.editor_toolbar)
    setSupportActionBar(toolbar)

    appBarConfig = AppBarConfiguration(
      setOf(R.id.nav_mix_up, R.id.nav_more_apps),
      findViewById<DrawerLayout>(R.id.drawer_layout))

    findNavController(R.id.nav_host_fragment).also {
      it.addOnDestinationChangedListener(this)
      setupActionBarWithNavController(it, appBarConfig)
      findViewById<NavigationView>(R.id.nav_view).setupWithNavController(it)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    this.menu = menu
    menuInflater.inflate(R.menu.main_options_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_share -> return shareImage()
      R.id.action_save -> return saveImageToGallery()
      R.id.action_reset -> return resetImage()
    }
    return false
  }

  private fun shareImage(): Boolean {
    val view = findViewById<FrameLayout>(R.id.image_container)

    if (view != null) {
      findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.VISIBLE
      ImageUtils(this, this).prepareViewForSharing(view)
    }
    else toastErrorAndLog("Share failed. Unable to located image container.")
    return true
  }

  override fun onReadyToShareImage(uri: Uri?) {
    findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.GONE

    if (uri != null) startShareIntent(uri)
    else toastErrorAndLog("Share failed.")
  }

  private fun startShareIntent(uri: Uri?) {
    val shareIntent: Intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      type = "image/jpeg"
    }
    startActivity(Intent.createChooser(shareIntent, "Share to..."))
  }

  private fun saveImageToGallery(): Boolean {
    val view = findViewById<FrameLayout>(R.id.image_container)

    if (view != null) {
      findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.VISIBLE
      ImageUtils(this, this).saveViewToGallery(view)
    }
    else toastErrorAndLog("Save failed. Unable to located image container.")
    return true
  }

  override fun onImageSavedToGallery(isSuccess: Boolean) {
    findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.GONE

    if (isSuccess) Toast.makeText(this, "Saved to gallery!", Toast.LENGTH_SHORT).show()
    else toastErrorAndLog("Write to gallery failed.", "Not saved.")
  }

  private fun resetImage(): Boolean {
    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_mix_up)
    return true
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
  }

  private fun toastErrorAndLog(logMsg: String, toastMsg: String = "Something went wrong :(") {
    Log.d(LOG_TAG, logMsg)
    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()
  }

  override fun onDestinationChanged(controller: NavController, dest: NavDestination, args: Bundle?) {
    val showOptionsMenu = dest.id != R.id.nav_more_apps
    menu?.setGroupVisible(R.id.main_options_menu_group, showOptionsMenu)
  }
}