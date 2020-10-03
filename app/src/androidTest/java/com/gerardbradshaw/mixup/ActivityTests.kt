package com.gerardbradshaw.mixup

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gerardbradshaw.mixup.ActivityTestUtil.checkOptionsMenuVisibility
import com.gerardbradshaw.mixup.ActivityTestUtil.closeDrawer
import com.gerardbradshaw.mixup.ActivityTestUtil.openDrawerAndNavToEditorFragment
import com.gerardbradshaw.mixup.ActivityTestUtil.openDrawerAndNavToMoreAppsFragment
import com.gerardbradshaw.mixup.ui.MainActivity
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