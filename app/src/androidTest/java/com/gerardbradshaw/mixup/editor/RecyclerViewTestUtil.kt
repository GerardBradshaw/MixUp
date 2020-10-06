package com.gerardbradshaw.mixup.editor

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.util.Checks
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.ui.editor.CollageLayoutListAdapter
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Assert

object RecyclerViewTestUtil {

  fun checkRecyclerViewContainsLayoutOptions() {
    onRecyclerView()
      .check(matches(atPosition(0, hasDescendant(withId(R.id.list_item_button)))))
  }

  fun checkRecyclerViewContainsAspectRatioOptions() {
    onRecyclerView()
      .check(matches(atPosition(0, hasDescendant(withId(R.id.aspect_ratio_text_view)))))
  }

  fun clickRecyclerViewAtPosition(position: Int) {
    onRecyclerView()
      .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click()))
  }




  // ---------------- UTIL ----------------

  private fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?>? {
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

  private fun onRecyclerView() : ViewInteraction {
    return onView(allOf(withId(R.id.tool_popup_recycler), isDisplayed()))
  }
}