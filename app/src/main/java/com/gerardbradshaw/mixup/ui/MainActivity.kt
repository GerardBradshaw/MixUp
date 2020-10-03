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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.gerardbradshaw.collageview.util.ImageUtil
import com.gerardbradshaw.collageview.views.AbstractCollageView
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.ui.editor.EditorFragment
import com.google.android.material.navigation.NavigationView

class MainActivity :
  AppCompatActivity(),
  ImageUtil.ImageSavedListener,
  NavController.OnDestinationChangedListener {

  // -------------------- PROPERTIES --------------------

  private lateinit var appBarConfig: AppBarConfiguration
  private var menu: Menu? = null
  private var collageContainer: FrameLayout? = null
  private var progressBarContainer: FrameLayout? = null

  var savedImageUri: Uri? = null



  // -------------------- INIT --------------------

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Nothing to inject here! But this is the code I'd use:
    //(application as BaseApplication).getAppComponent().activityComponent().create().inject(this)

    initUi()
  }

  private fun initUi() {
    setSupportActionBar(findViewById(R.id.editor_toolbar))
    initNavigation()
  }

  private fun initNavigation() {
    appBarConfig = AppBarConfiguration(
      setOf(R.id.nav_editor, R.id.nav_more_apps),
      findViewById<DrawerLayout>(R.id.drawer_layout))

    findNavController(R.id.nav_host_fragment).also {
      it.addOnDestinationChangedListener(this)
      setupActionBarWithNavController(it, appBarConfig)
      findViewById<NavigationView>(R.id.nav_view).setupWithNavController(it)
    }
  }



  // -------------------- NAVIGATION --------------------

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    this.menu = menu
    menuInflater.inflate(R.menu.main_options_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onDestinationChanged(controller: NavController, dest: NavDestination, args: Bundle?) {
    val showOptionsMenu = dest.id != R.id.nav_more_apps
    menu?.setGroupVisible(R.id.main_options_menu_group, showOptionsMenu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (getCurrentFragment() !is EditorFragment) {
      Log.d(TAG, "onOptionsItemSelected: current fragment is not the editor!")
      return super.onOptionsItemSelected(item)
    }

    when (item.itemId) {
      R.id.action_share -> shareImage()
      R.id.action_save -> saveCollageToGallery()
      R.id.action_reset -> resetImage()
      else -> return false
    }
    return true
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
  }



  // -------------------- SHARE COLLAGE --------------------

  private fun shareImage() {
    prepareToCaptureCollage()
    ImageUtil.prepareViewForSharing(this, collageContainer!!, this)
  }

  override fun onReadyToShareImage(uri: Uri?) {
    setProgressBarVisibility(View.GONE)

    if (uri == null) toastErrorAndLog("Unable to share.", "Unable to share!")
    else {
      savedImageUri = uri
      startShareIntent(uri)
    }
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



  // -------------------- SAVE COLLAGE --------------------

  private fun saveCollageToGallery() {
    prepareToCaptureCollage()
    ImageUtil.saveViewToGallery(this, collageContainer!!, this)
  }

  override fun onCollageSavedToGallery(isSaveSuccessful: Boolean, uri: Uri?) {
    setProgressBarVisibility(View.GONE)

    savedImageUri = uri

    if (isSaveSuccessful) {
      Toast.makeText(this, "Saved to gallery!", Toast.LENGTH_LONG).show()
    }
    else toastErrorAndLog("Write to gallery failed.", "Unable to save :(")
  }



  // -------------------- UTIL --------------------

  private fun resetImage() {
    if (collageContainer == null) collageContainer = findViewById(R.id.collage_container)
    (collageContainer!!.getChildAt(0) as AbstractCollageView).reset()
    (getCurrentFragment() as EditorFragment).reset()
  }

  private fun getCurrentFragment(): Fragment {
    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
    return navHostFragment!!.childFragmentManager.fragments[0]
  }

  private fun prepareToCaptureCollage() {
    savedImageUri = null
    setProgressBarVisibility(View.VISIBLE)
    if (collageContainer == null) collageContainer = findViewById(R.id.collage_container)
  }

  private fun setProgressBarVisibility(visibility: Int) {
    if (progressBarContainer == null) {
      progressBarContainer = findViewById(R.id.progress_bar_container)
    }

    progressBarContainer?.visibility = visibility
  }

  private fun toastErrorAndLog(logMsg: String, toastMsg: String = "Something went wrong :(") {
    Log.d(TAG, "toastErrorAndLog: $logMsg")
    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
  }

  companion object {
    private const val TAG = "MainActivity"
  }
}