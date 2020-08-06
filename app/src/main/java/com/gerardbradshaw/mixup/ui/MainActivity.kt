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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gerardbradshaw.mixup.utils.ImageUtils
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.ui.moreapps.MoreAppsFragment
import com.google.android.material.navigation.NavigationView

private const val LOG_TAG = "MainActivity"

class MainActivity : AppCompatActivity(), MoreAppsFragment.OnFragmentCreatedListener,
  ImageUtils.ImageSavedListener {

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
      setOf(
        R.id.nav_mix_up,
        R.id.nav_more_apps
      ), drawerLayout)
    setupActionBarWithNavController(navigationController, appBarConfiguration)
    navigationView.setupWithNavController(navigationController)
  }

  override fun onFragmentChanged(shouldShowOptionsMenu: Boolean) {
    menu?.setGroupVisible(R.id.main_options_menu_group, shouldShowOptionsMenu)
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
      R.id.action_reset -> resetImage()
    }
    return super.onOptionsItemSelected(item)
  }

  private fun shareImage() {
    val view = findViewById<FrameLayout>(R.id.image_container)

    if (view != null) {
      findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.VISIBLE
      ImageUtils(this, this).prepareViewForSharing(view)
    }
    else toastErrorAndLog("Share failed. Unable to located image container.")
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

  private fun saveImageToGallery() {
    val view = findViewById<FrameLayout>(R.id.image_container)

    if (view != null) {
      findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.VISIBLE
      ImageUtils(this, this).saveViewToGallery(view)
    }
    else toastErrorAndLog("Save failed. Unable to located image container.")
  }

  override fun onImageSavedToGallery(isSuccess: Boolean) {
    findViewById<FrameLayout>(R.id.progress_bar_frame).visibility = View.GONE

    if (isSuccess) Toast.makeText(this, "Saved to gallery!", Toast.LENGTH_SHORT).show()
    else toastErrorAndLog("Write to gallery failed.", "Not saved.")
  }

  private fun resetImage() {
    val navController = findNavController(R.id.nav_host_fragment)
    navController.navigate(R.id.nav_mix_up)
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }

  private fun toastErrorAndLog(logMsg: String, toastMsg: String = "Something went wrong :(") {
    Log.d(LOG_TAG, logMsg)
    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()
  }
}