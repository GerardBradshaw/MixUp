package com.gerardbradshaw.mixup.editor

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
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

  fun onRecyclerView() : ViewInteraction {
    return onView(allOf(withId(R.id.tool_popup_recycler), isDisplayed()))
  }

  fun checkRecyclerViewContainsLayoutOptions() {
    onRecyclerView()
      .check(matches(atPosition(0, hasDescendant(withId(R.id.list_item_button)))))
  }

  fun checkRecyclerViewContainsAspectRatioOptions() {
    onRecyclerView()
      .check(matches(atPosition(0, hasDescendant(withId(R.id.aspect_ratio_text_view)))))
  }



  private class RecyclerViewContainsLayouts : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) throw noViewFoundException

      val adapter = (view as RecyclerView).adapter
      val isLayoutsAdapter = adapter is CollageLayoutListAdapter
      Assert.assertTrue(isLayoutsAdapter)
    }
  }


  private fun hasAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): Matcher<RecyclerView?>? {
    Checks.checkNotNull(adapter)

    return object : BoundedMatcher<RecyclerView?, RecyclerView>(RecyclerView::class.java) {
      override fun matchesSafely(recycler: RecyclerView): Boolean {
        val recyclerAdapter = recycler.adapter?.getItemViewType(0)
        return false // (recycler.adapter) is test
      }

      override fun describeTo(description: Description) {
        description.appendText("with background color")
      }
    }
  }
}