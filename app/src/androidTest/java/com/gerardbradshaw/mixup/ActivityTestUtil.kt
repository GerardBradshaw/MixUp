package com.gerardbradshaw.mixup

import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*

object ActivityTestUtil {

  fun checkOptionsMenuVisibility(expectedIsVisible: Boolean) {
    val visibility = if (expectedIsVisible) matches(isDisplayed()) else doesNotExist()

    onView(withId(R.id.action_share)).check(visibility)
    onView(withId(R.id.action_save)).check(visibility)
    onView(withId(R.id.action_reset)).check(visibility)
  }

  fun openDrawerAndNavToMoreAppsFragment() {
    openDrawer()
    navigateToMoreAppsFragmentFromOpenDrawer()
  }

  fun openDrawerAndNavToEditorFragment() {
    openDrawer()
    navigateToEditorFragmentFromOpenDrawer()
  }

  fun closeDrawer() {
    onView(withId(R.id.drawer_layout))
      .perform(DrawerActions.close())
  }

  fun pressOptionsMenuButton(buttonId: Int) {
    onView(withId(buttonId))
      .perform(ViewActions.click())
  }



  // ---------------- UTIL ----------------

  private fun openDrawer() {
    onView(withId(R.id.drawer_layout))
      .check(matches(isClosed(Gravity.LEFT)))
      .perform(DrawerActions.open())
  }

  private fun navigateToMoreAppsFragmentFromOpenDrawer() {
    onView(withId(R.id.nav_view))
      .perform(NavigationViewActions.navigateTo(R.id.nav_more_apps))
  }

  private fun navigateToEditorFragmentFromOpenDrawer() {
    onView(withId(R.id.nav_view))
      .perform(NavigationViewActions.navigateTo(R.id.nav_editor))
  }
}