package com.gerardbradshaw.v2mixup.editor

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import com.gerardbradshaw.collageview.views.AbstractCollageView
import com.gerardbradshaw.colorpickerlibrary.views.CompactColorPickerView
import com.gerardbradshaw.v2mixup.ActivityTestUtil.getRandomImageUri
import com.gerardbradshaw.v2mixup.R
import com.gerardbradshaw.v2mixup.TestUtil
import com.gerardbradshaw.v2mixup.TestUtil.nthChildOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*


object EditorTestUtil {

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

  fun checkCollageHasActualAspectRatio(expectedRatio: Float) {
    onCollageView()
      .check(matches(hasActualAspectRatio(expectedRatio)))
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

  fun setCollageAspectRatio(ratio: Float) {
    onCollageView()
      .perform(setCollageAspectRatioAction(ratio))
  }

  private fun setCollageAspectRatioAction(ratio: Float): ViewAction? {
    return object : ViewAction {
      override fun getDescription(): String {
        return "change aspect ratio"
      }

      override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(AbstractCollageView::class.java)
      }

      override fun perform(uiController: UiController?, view: View?) {
        if (view is AbstractCollageView) view.aspectRatio = ratio
      }
    }
  }



  // ---------------- BORDER ----------------

  fun checkCollageBorderEnabled(expected: Boolean) {
    onCollageView()
      .check(matches(isBorderEnabled(expected)))
  }

  private fun isBorderEnabled(expected: Boolean): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        return view.isBorderEnabled == expected
      }

      override fun describeTo(description: Description) {
        description.appendText("with view tag")
      }
    }
  }

  fun setPickerColorRatio(ratio: Double) {
    onView(withId(R.id.color_picker_view))
      .perform(setColorRatio(ratio))
  }

  private fun setColorRatio(ratio: Double): ViewAction? {
    return object : ViewAction {
      override fun getDescription(): String {
        return "set border color"
      }

      override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(CompactColorPickerView::class.java)
      }

      override fun perform(uiController: UiController?, view: View?) {
        if (view is CompactColorPickerView) {
          view.colorRatio = ratio
        }
      }
    }
  }

  fun checkBorderColorIs(color: Int) {
    onCollageView()
      .check(matches(hasBorderColor(color)))
  }

  private fun hasBorderColor(color: Int): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        return view.getBorderColor() == color
      }

      override fun describeTo(description: Description) {
        description.appendText("with view tag")
      }
    }
  }



  // ---------------- COLLAGE VIEW IMAGES ----------------

  private fun onCollageView(): ViewInteraction {
    return onView(nthChildOf(withId(R.id.collage_container), 0))
  }

  fun changeAllImages(imageCount: Int, activity: Activity) {
    for (i in 0 until imageCount) changeImageAt(i, activity)
  }

  fun changeImageAt(position: Int, activity: Activity) {
    Intents.init()

    val intent = Intent()
    intent.data = getRandomImageUri(activity)

    val activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

    intending(not(isInternal()))
      .respondWith(activityResult)

    clickImageAtPosition(position)

    Intents.release()
  }

  fun clickImageAtPosition(position: Int) {
    onCollageView()
      .perform(clickImageAtPositionAction(position))
  }

  private fun clickImageAtPositionAction(position: Int): ViewAction? {
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

  fun checkImageAtPositionIsNotTheDefault(position: Int) {
    onCollageView()
      .check(matches(isNotDefaultImageAt(position)))
  }

  private fun isNotDefaultImageAt(position: Int): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        return view.isSetImageAt(position)
      }

      override fun describeTo(description: Description) {
        description.appendText("with valid image count equal to image count")
      }
    }
  }

  fun checkAllImagesAreTheDefault() {
    onCollageView()
      .check(matches(hasAllDefaultImages()))
  }

  private fun hasAllDefaultImages(): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        Log.d("GGG", "${view.getSetImageCount()} valid images")
        return view.getSetImageCount() == 0
      }

      override fun describeTo(description: Description) {
        description.appendText("with valid image count equal to image count")
      }
    }
  }

  fun checkAllImagesAreNotTheDefault() {
    onCollageView()
      .check(matches(hasNoDefaultImages()))
  }

  private fun hasNoDefaultImages(): Matcher<View?>? {
    return object : BoundedMatcher<View?, AbstractCollageView>(AbstractCollageView::class.java) {
      override fun matchesSafely(view: AbstractCollageView): Boolean {
        return view.getSetImageCount() == view.imageCount()
      }

      override fun describeTo(description: Description) {
        description.appendText("with valid image count equal to image count")
      }
    }
  }



  // ---------------- RECYCLER VIEW ----------------

  private fun onRecyclerView() : ViewInteraction {
    return onView(allOf(withId(R.id.tool_popup_recycler), isDisplayed()))
  }

  fun checkRecyclerViewContainsLayoutOptions() {
    onRecyclerView()
      .check(matches(TestUtil.atPosition(0, hasDescendant(withId(R.id.list_item_button)))))
  }

  fun checkRecyclerViewContainsAspectRatioOptions() {
    onRecyclerView()
      .check(matches(TestUtil.atPosition(0, hasDescendant(withId(R.id.aspect_ratio_text_view)))))
  }

  fun clickRecyclerViewAtPosition(position: Int) {
    onRecyclerView()
      .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))

    onRecyclerView()
      .perform(
        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
          position,
          ViewActions.click()
        )
      )
  }



  // ---------------- COLLAGE TYPE ----------------

  fun <V> checkCollageTypeIs(collageType: Class<V>) {
    onCollageView()
      .check(matches(instanceOf(collageType)))
  }
}