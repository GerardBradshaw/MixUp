package com.gerardbradshaw.mixup.editor

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import com.gerardbradshaw.collageview.views.AbstractCollageView
import com.gerardbradshaw.mixup.ActivityTestUtil.getRandomImageUri
import com.gerardbradshaw.mixup.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher

object CollageViewTestUtil {

  // ---------------- ASPECT RATIO ----------------

  fun checkCollageHasAspectRatioSetTo(expectedRatio: Float?) {
    onCollageView()
      .check(matches(hasAspectRatioSetTo(expectedRatio)))
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
    onCollageView()
      .check(matches(hasAspectRatioSetTo(expectedRatio)))
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



  // ---------------- IMAGES ----------------

  fun changeAllImages(imageCount: Int, activity: Activity) {
    for (i in 0 until imageCount) changeImageAt(i, activity)
  }

  fun changeImageAt(position: Int, activity: Activity) {
    Intents.init()

    val intent = Intent()
    intent.data = getRandomImageUri(activity)
//    Uri.parse("android.resource://com.gerardbradshaw.mixup/drawable/mater_logo")

    val activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

    intending(not(isInternal()))
      .respondWith(activityResult)

    clickCollageImageAt(position)

    Intents.release()
  }

  private fun clickCollageImageAt(position: Int) {
    onCollageView()
      .perform(clickImageAt(position))
  }

  private fun onCollageView(): ViewInteraction {
    return onView(nthChildOf(withId(R.id.collage_container), 0))
  }

  private fun clickImageAt(position: Int): ViewAction? {
    return object : ViewAction {
      override fun getDescription(): String {
        return "click image at position"
      }

      override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(AbstractCollageView::class.java)
      }

      override fun perform(uiController: UiController?, view: View?) {
        if (view is AbstractCollageView) view.getChildAt(position).performClick()
      }
    }
  }

  fun checkAllImagesIsDefault(expectedIsDefault: Boolean) {
    onCollageView()
      .check(matches(hasNoDefaultImages(expectedIsDefault)))
  }

  private fun hasNoDefaultImages(expectedIsDefault: Boolean): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        val isDefault = view.getValidImageCount() == view.imageCount()
        return isDefault == expectedIsDefault
      }

      override fun describeTo(description: Description) {
        description.appendText("with valid image count equal to image count")
      }
    }
  }



  // ---------------- COLLAGE TYPE ----------------

  fun <V> checkCollageTypeIs(collageType: Class<V>) {
    onCollageView()
      .check(matches(instanceOf(collageType)))
  }



  // ---------------- UTIL ----------------

  private fun nthChildOf(parentMatcher: Matcher<View?>, childPosition: Int): Matcher<View?>? {
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


}