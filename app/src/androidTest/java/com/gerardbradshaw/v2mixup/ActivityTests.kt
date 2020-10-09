package com.gerardbradshaw.v2mixup

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gerardbradshaw.v2mixup.ActivityTestUtil.checkOptionsMenuVisibility
import com.gerardbradshaw.v2mixup.ActivityTestUtil.closeDrawer
import com.gerardbradshaw.v2mixup.ActivityTestUtil.openDrawerAndNavToEditorFragment
import com.gerardbradshaw.v2mixup.ActivityTestUtil.openDrawerAndNavToMoreAppsFragment
import com.gerardbradshaw.v2mixup.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityTests {

  @Rule
  @JvmField
  val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

  @Test
  fun should_showOptionsMenuInEditorFragment_when_returningFromMoreAppsFragment() {
    openDrawerAndNavToMoreAppsFragment()
    closeDrawer()
    openDrawerAndNavToEditorFragment()
    checkOptionsMenuVisibility(true)
  }

  @Test
  fun should_hideOptionsMenu_when_inMoreAppsFragment() {
    openDrawerAndNavToMoreAppsFragment()
    checkOptionsMenuVisibility(false)
  }
}