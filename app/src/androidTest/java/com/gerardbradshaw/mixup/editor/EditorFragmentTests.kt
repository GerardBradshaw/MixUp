package com.gerardbradshaw.mixup.editor

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.gerardbradshaw.collageview.views.CollageView3Image2
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.checkCollageHasAspectRatioSetTo
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.checkCollageIsBorderEnabled
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.ActivityTestUtil.checkOptionsMenuVisibility
import com.gerardbradshaw.mixup.ActivityTestUtil.openDrawerAndNavToEditorFragment
import com.gerardbradshaw.mixup.ActivityTestUtil.openDrawerAndNavToMoreAppsFragment
import com.gerardbradshaw.mixup.ActivityTestUtil.pressOptionsMenuButton
import com.gerardbradshaw.mixup.editor.RecyclerViewTestUtil.checkRecyclerViewContainsLayoutOptions
import com.gerardbradshaw.mixup.editor.RecyclerViewTestUtil.checkRecyclerViewContainsAspectRatioOptions
import com.gerardbradshaw.mixup.ui.MainActivity
import org.hamcrest.Matchers
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Enclosed::class)
class EditorFragmentTests {

  @RunWith(AndroidJUnit4::class)
  class InitializationTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_useAppContext_when_launched() {
      val appContext = InstrumentationRegistry.getInstrumentation().targetContext
      assertEquals("com.gerardbradshaw.mixup", appContext.packageName)
    }

    @Test
    fun should_showLayoutOptions_when_firstEntering() {
      checkRecyclerViewContainsLayoutOptions()
    }

    @Test
    fun should_haveNoDefinedAspectRatio_when_firstEntering() {
      checkCollageHasAspectRatioSetTo(null)
    }

    @Test
    fun should_showOptionsMenu_when_firstEntering() {
      checkOptionsMenuVisibility(true)
    }

    @Test
    fun should_startCollageWithNoBorder_when_firstEntering() {
      checkCollageIsBorderEnabled(false)
    }

    @Test
    fun should_startCollageWith3image2layout_when_firstEntering() {
      onView(Matchers.allOf(withId(R.id.collage_container)))
        .check(matches(withChild(instanceOf(CollageView3Image2::class.java))))
    }
  }


  @RunWith(AndroidJUnit4::class)
  class IOTests {

//    @get:Rule
//    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @get:Rule
    val atr = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    fun should_saveCollageToGallery_when_saveButtonPressed() {
      // Save the collage
      pressOptionsMenuButton(R.id.action_save)

      // Get the filename of the saved image
      val filename = atr.activity
      // TODO

      // Open the gallery
      // TODO

      // Check that the gallery contains an image with same filename
      // TODO

      val galleryResult = atr.activity
      TODO()
    }

    @Test
    fun should_shareCollage_when_shareButtonPressed() {
      TODO()
    }
  }


  @RunWith(AndroidJUnit4::class)
  class CollageLayoutTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_showLayoutOptions_when_layoutButtonClicked() {
      onView(withId(R.id.button_layout))
        .perform(click())

      checkRecyclerViewContainsLayoutOptions()
    }

    @Test
    fun should_loadPreviouslySelectedImages_when_layoutChanged() {
      TODO()
    }

    @Test
    fun should_resetCollage_when_resetButtonPressed() {
      TODO()
    }
  }


  @RunWith(Parameterized::class)
  class ParameterizedCollageLayoutTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_changeLayout_when_newLayoutSelected() {
      TODO()
    }

    companion object {
      @Parameterized.Parameters(name = "layout = {0}")
      @JvmStatic
      fun params(): Collection<Array<Any>> {
        TODO()
      }
    }
  }


  @RunWith(AndroidJUnit4::class)
  class AspectRatioTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_showAspectRatioOptions_when_aspectRatioButtonClicked() {
      onView(withId(R.id.button_aspect_ratio))
        .perform(click())

      checkRecyclerViewContainsAspectRatioOptions()
    }

    @Test
    fun should_loadPreviouslySelectedImages_when_aspectRatioChanged() {
      TODO()
    }
  }


  @RunWith(Parameterized::class)
  class ParameterizedAspectRatioTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_changeAspectRatio_when_newAspectRatioSelected() {
      TODO()
    }

    companion object {
      @Parameterized.Parameters(name = "ratio = {0}")
      @JvmStatic
      fun params(): Collection<Array<Any>> {
        TODO()
      }
    }
  }


  @RunWith(AndroidJUnit4::class)
  class BorderTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_showBorderOptions_when_borderButtonPressed() {
      TODO()
    }

    @Test
    fun should_ignoreBorderButtonPress_when_alreadyShowingBorderOptions() {
      TODO()
    }

    @Test
    fun should_startWithBorderSwitchOff_when_firstEnteringBorderOptions() {
      TODO()
    }

    @Test
    fun should_enableBorder_when_switchTurnedOnFromOffState() {
      TODO()
    }

    @Test
    fun should_disableBorder_when_switchTurnedOffFromOnState() {
      TODO()
    }

    @Test
    fun should_enableBorderSwitch_when_newBorderColorSelected() {
      TODO()
    }

    @Test
    fun should_changeBorderColor_when_newBorderColorSelected() {
      TODO()
    }
  }


  @RunWith(AndroidJUnit4::class)
  class CollageTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_openGalleryApp_when_imageClicked() {
      TODO()
    }

    @Test
    fun should_importImageIntoCollage_when_imageSelectedFromGallery() {
      TODO()
    }

  }
}