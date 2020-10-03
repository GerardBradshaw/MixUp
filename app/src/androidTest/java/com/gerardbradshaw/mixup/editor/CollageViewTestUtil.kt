package com.gerardbradshaw.mixup.editor

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.gerardbradshaw.collageview.views.AbstractCollageView
import com.gerardbradshaw.mixup.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*

object CollageViewTestUtil {

  // ---------------- ASPECT RATIO ----------------

  fun checkCollageHasAspectRatioSetTo(expectedRatio: Float?) {
    onView(allOf(withId(R.id.collage_container)))
      .check(matches(withChild(hasAspectRatioSetTo(expectedRatio))))
  }

  private fun hasAspectRatioSetTo(expectedRatio: Float?): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        return view.aspectRatio == expectedRatio
      }

      override fun describeTo(description: Description) {
        description.appendText("with view tag")
      }
    }
  }

  fun checkCollageHasActualAspectRatio(expectedRatio: Float?) {
    onView(allOf(withId(R.id.collage_container)))
      .check(matches(withChild(hasAspectRatioSetTo(expectedRatio))))
  }

  private fun hasActualAspectRatio(expectedRatio: Float): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        val aspectRatio = view.width.toFloat() / view.height.toFloat()
        return aspectRatio in (0.99 * expectedRatio)..(1.01 * expectedRatio)
      }

      override fun describeTo(description: Description) {
        description.appendText("with view tag")
      }
    }
  }



  // ---------------- BORDER ----------------

  fun checkCollageIsBorderEnabled(expectedIsBorderEnabled: Boolean) {
    onView(allOf(withId(R.id.collage_container)))
      .check(matches(withChild(hasIsBorderEnabled(expectedIsBorderEnabled))))
  }

  private fun hasIsBorderEnabled(expectedResult: Boolean): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        return view.isBorderEnabled == expectedResult
      }

      override fun describeTo(description: Description) {
        description.appendText("with view tag")
      }
    }
  }



  // ---------------- COLLAGE TYPE ----------------

  fun <V : Class<AbstractCollageView>> checkCollageTypeIs(collageType: V) {
    onView(allOf(withId(R.id.collage_container)))
      .check(matches(withChild(instanceOf(collageType))))
  }

}