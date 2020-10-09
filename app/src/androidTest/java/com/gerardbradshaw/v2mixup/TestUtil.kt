package com.gerardbradshaw.v2mixup

import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.*
import org.hamcrest.Matchers.not

object TestUtil {

  fun checkIsDisplayed(resId: Int) {
    onView(withId(resId))
      .check(matches(isDisplayed()))
  }

  fun checkIsNotDisplayed(resId: Int) {
    onView(withId(resId))
      .check(matches(not(isDisplayed())))
  }

  fun nthChildOf(parentMatcher: Matcher<View?>, childPosition: Int): Matcher<View?>? {
    return object : TypeSafeMatcher<View?>() {
      override fun describeTo(description: Description) {
        description.appendText("with $childPosition child view of type parentMatcher")
      }

      override fun matchesSafely(view: View?): Boolean {
        if (view != null) {
          if (view.parent !is ViewGroup) {
            return parentMatcher.matches(view.parent)
          }
          val group: ViewGroup = view.parent as ViewGroup
          return parentMatcher.matches(view.parent) && group.getChildAt(childPosition) == view
        }
        return false
      }
    }
  }

  fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?>? {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
      override fun describeTo(description: Description) {
        description.appendText("has item at position $position: ")
        itemMatcher.describeTo(description)
      }

      override fun matchesSafely(view: RecyclerView): Boolean {
        val viewHolder = view.findViewHolderForAdapterPosition(position) ?: return false
        return itemMatcher.matches(viewHolder.itemView)
      }
    }
  }

  fun setChecked(checked: Boolean): ViewAction? {
    return object : ViewAction {
      override fun getDescription(): String {
        return "set checked"
      }

      override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(SwitchCompat::class.java)
      }

      override fun perform(uiController: UiController, view: View) {
        val checkableView: Checkable = view as Checkable
        checkableView.isChecked = checked
      }
    }
  }
}