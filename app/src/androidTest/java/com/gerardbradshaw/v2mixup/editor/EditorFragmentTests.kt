package com.gerardbradshaw.v2mixup.editor

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.gerardbradshaw.collageview.views.*
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkCollageHasAspectRatioSetTo
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkCollageBorderEnabled
import com.gerardbradshaw.v2mixup.R
import com.gerardbradshaw.v2mixup.ActivityTestUtil.checkOptionsMenuVisibility
import com.gerardbradshaw.v2mixup.ActivityTestUtil.countImagesOnDevice
import com.gerardbradshaw.v2mixup.ActivityTestUtil.pressOptionsMenuButton
import com.gerardbradshaw.v2mixup.BaseApplication
import com.gerardbradshaw.v2mixup.TestUtil.checkIsDisplayed
import com.gerardbradshaw.v2mixup.TestUtil.checkIsNotDisplayed
import com.gerardbradshaw.v2mixup.TestUtil.setChecked
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.changeAllImages
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.changeImageAt
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkAllImagesAreTheDefault
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkAllImagesAreNotTheDefault
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkBorderColorIs
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkCollageHasActualAspectRatio
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkCollageTypeIs
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkImageAtPositionIsNotTheDefault
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkRecyclerViewContainsLayoutOptions
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.checkRecyclerViewContainsAspectRatioOptions
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.clickImageAtPosition
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.clickRecyclerViewAtPosition
import com.gerardbradshaw.v2mixup.editor.EditorTestUtil.setPickerColorRatio
import com.gerardbradshaw.v2mixup.ui.MainActivity
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Enclosed::class)
class EditorFragmentTests {

  // ---------------- INITIALIZATION TESTS ----------------

  @RunWith(AndroidJUnit4::class)
  class InitializationTests {
    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_useAppContext_when_launched() {
      val appContext = InstrumentationRegistry.getInstrumentation().targetContext
      assertEquals("com.gerardbradshaw.v2mixup", appContext.packageName)
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
      checkCollageBorderEnabled(false)
    }

    @Test
    fun should_startCollageWith3image2layout_when_firstEntering() {
      checkCollageTypeIs(CollageView3Image2::class.java)
    }
  }



  // ---------------- IO TESTS ----------------

  @RunWith(AndroidJUnit4::class)
  class IOTests {
    lateinit var activityScenario: ActivityScenario<MainActivity>
    lateinit var activity: MainActivity

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
      ApplicationProvider.getApplicationContext<BaseApplication>().replaceDispatchersForTests()

      activityScenario = ActivityScenario.launch(MainActivity::class.java)

      activityScenario.onActivity {
        activity = it
      }
    }

    @Test
    fun should_saveCollageToGallery_when_saveButtonPressed() {
      val initialImageCount = countImagesOnDevice(activity)
      pressOptionsMenuButton(R.id.action_save)
      assertEquals(initialImageCount + 1, countImagesOnDevice(activity))
    }

    @Test
    fun should_shareCollage_when_shareButtonPressed() {
      Intents.init()

      pressOptionsMenuButton(R.id.action_share)

      intended(allOf(hasAction(Intent.ACTION_CHOOSER), hasExtra(`is`(Intent.EXTRA_INTENT),
        allOf(hasAction(Intent.ACTION_SEND), hasExtraWithKey(Intent.EXTRA_STREAM)))))

      Intents.release()
    }
  }



  // ---------------- LAYOUT TESTS ----------------

  @RunWith(AndroidJUnit4::class)
  class CollageLayoutTests {
    lateinit var activityScenario: ActivityScenario<MainActivity>
    lateinit var activity: MainActivity

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
      activityScenario = ActivityScenario.launch(MainActivity::class.java)

      activityScenario.onActivity {
        activity = it
      }
    }

    @Test
    fun should_showLayoutOptions_when_layoutButtonClicked() {
      onView(withId(R.id.button_layout)).perform(click())
      checkRecyclerViewContainsLayoutOptions()
    }

    @Test
    fun should_loadPreviouslySelectedImages_when_layoutChanged() {
      changeAllImages(3, activity)
      clickRecyclerViewAtPosition(3)
      checkAllImagesAreNotTheDefault()
    }

    @Test
    fun should_notChangeCollageType_when_resetButtonPressed() {
      clickRecyclerViewAtPosition(3)
      pressOptionsMenuButton(R.id.action_reset)
      checkCollageTypeIs(CollageView3Image1::class.java)
    }

    @Test
    fun should_haveDefaultImages_when_resetButtonPressed() {
      changeAllImages(3, activity)
      pressOptionsMenuButton(R.id.action_reset)
      checkAllImagesAreTheDefault()
    }
  }


  @RunWith(Parameterized::class)
  class ParameterizedLayoutChangeTests(
    private val inputRecyclerPosition: Int,
    private val expectedViewType: Class<AbstractCollageView>
  ) {
    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_changeLayout_when_newLayoutSelected() {
      clickRecyclerViewAtPosition(inputRecyclerPosition)
      checkCollageTypeIs(expectedViewType)
    }

    companion object {
      @Parameterized.Parameters(name = "layout position in recycler = {0}")
      @JvmStatic
      fun params(): Collection<Array<Any>> {
        val expectedOutputs = arrayOf<Any>(
          CollageViewVertical::class.java,
          CollageViewHorizontal::class.java,
          CollageView3Image0::class.java,
          CollageView3Image1::class.java,
          CollageView3Image2::class.java,
          CollageView3Image3::class.java,
          CollageViewHorizontal::class.java,
          CollageViewVertical::class.java,
          CollageView4Image0::class.java,
          CollageView4Image1::class.java,
          CollageView4Image2::class.java,
          CollageView4Image3::class.java,
          CollageView4Image4::class.java)

        val inputParams = Array<Any>(expectedOutputs.size) { it }

        return Array(inputParams.size) {
          arrayOf(inputParams[it], expectedOutputs[it])
        }.asList()
      }
    }
  }



  // ---------------- ASPECT RATIO TESTS ----------------

  @RunWith(AndroidJUnit4::class)
  class AspectRatioTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_showAspectRatioOptions_when_aspectRatioButtonClicked() {
      pressOptionsMenuButton(R.id.button_aspect_ratio)
      checkRecyclerViewContainsAspectRatioOptions()
    }
  }


  @RunWith(Parameterized::class)
  class ParameterizedAspectRatioTests(
    private val inputRecyclerPosition: Int,
    private val expectedOutputRatio: Float
  ) {
    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_changeAspectRatio_when_newAspectRatioSelected() {
      pressOptionsMenuButton(R.id.button_aspect_ratio)
      clickRecyclerViewAtPosition(inputRecyclerPosition)
      checkCollageHasActualAspectRatio(expectedOutputRatio)
    }

    companion object {
      @Parameterized.Parameters(name = "aspect ratio recycler position = {0}")
      @JvmStatic
      fun params(): Collection<Array<Any>> {
        val expectedOutputs = arrayOf<Any>(
          1f,
          16f / 9f,
          9f / 16f,
          10f / 8f,
          8f / 10f,
          7f / 5f,
          5f / 7f,
          4f / 3f,
          3f / 4f,
          5f / 3f,
          3f / 5f,
          3f / 2f,
          2f / 3f)

        val inputParams = Array<Any>(expectedOutputs.size) { it }

        return Array(inputParams.size) {
          arrayOf(inputParams[it], expectedOutputs[it])
        }.asList()
      }
    }
  }



  // ---------------- BORDER TESTS ----------------

  @RunWith(AndroidJUnit4::class)
  class BorderTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Before
    fun setup() {
      pressOptionsMenuButton(R.id.button_border)
    }

    @Test
    fun should_showBorderOptions_when_borderButtonPressed() {
      checkIsDisplayed(R.id.color_picker_container)
      checkIsNotDisplayed(R.id.tool_popup_recycler)
    }

    @Test
    fun should_startWithBorderSwitchOff_when_firstEnteringBorderOptions() {
      onView(withId(R.id.border_switch))
        .check(matches(isNotChecked()))
    }

    @Test
    fun should_enableBorder_when_switchTurnedOnFromOffState() {
      onView(withId(R.id.border_switch))
        .perform(setChecked(true))

      checkCollageBorderEnabled(true)
    }

    @Test
    fun should_disableBorder_when_switchTurnedOffFromOnState() {
      setPickerColorRatio(0.5)

      onView(withId(R.id.border_switch))
        .perform(setChecked(false))

      checkCollageBorderEnabled(false)
    }

    @Test
    fun should_enableBorderSwitch_when_newBorderColorSelected() {
      setPickerColorRatio(0.5)

      onView(withId(R.id.border_switch))
        .check(matches(isChecked()))
    }

    @Test
    fun should_changeBorderColor_when_newBorderColorSelected() {
      setPickerColorRatio(0.5)
      setPickerColorRatio(0.0)
      checkBorderColorIs(-65536)
    }
  }



  // ---------------- COLLAGE VIEW TEST ----------------

  @RunWith(AndroidJUnit4::class)
  class CollageTests {
    lateinit var activityScenario: ActivityScenario<MainActivity>
    lateinit var activity: MainActivity

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
      activityScenario = ActivityScenario.launch(MainActivity::class.java)

      activityScenario.onActivity {
        activity = it
      }
    }

    @Test
    fun should_openGalleryApp_when_imageClicked() {
      Intents.init()

      clickImageAtPosition(0)

      intended(hasAction(Intent.ACTION_PICK))

      Intents.release()
    }

    @Test
    fun should_importImageIntoPosition0_when_imageSelectedFromGallery() {
      changeImageAt(0, activity)
      checkImageAtPositionIsNotTheDefault(0)
    }
  }
}