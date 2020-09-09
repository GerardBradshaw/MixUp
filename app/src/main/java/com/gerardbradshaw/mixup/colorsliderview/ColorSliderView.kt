package com.gerardbradshaw.mixup.colorsliderview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.gerardbradshaw.mixup.R
import java.lang.String
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

class ColorSliderView : FrameLayout {

  // ------------------------ CONSTRUCTORS ------------------------

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


  // ------------------------ PROPERTIES ------------------------

  private lateinit var sliderMenu: LinearLayout
  private lateinit var sliderMenuTextView: TextView
  private lateinit var seekBar: SeekBar
  private lateinit var seekBarLine: FrameLayout
  private lateinit var preview: ImageView

  private var currentSlider: SliderType = SliderType.COLOR

  private var colorRatio = 0.0
  private var shadeRatio = 0.0
  private var tintRatio = 0.0

  var listener: ColorChangedListener? = null


  // ------------------------ INITIALIZATION ------------------------

  init {
    View.inflate(context, R.layout.picker_compact, this)

    initSliderMenu()
    initSeekBar()
    initBarLine()
    initPreview()
  }

  private fun initSliderMenu() {
    sliderMenuTextView = findViewById(R.id.text_view)

    sliderMenu = findViewById(R.id.selector_button)
    sliderMenu.setOnClickListener {
      val popup = PopupMenu(context, it)

      popup.menuInflater.inflate(R.menu.color_options, popup.menu)

      popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem?): Boolean {
          if (item == null) return false

          when (item.itemId) {
            R.id.option_color -> {
              currentSlider = SliderType.COLOR
              sliderMenuTextView.setText("Color")
            }
            R.id.option_shade -> {
              currentSlider = SliderType.SHADE
              sliderMenuTextView.setText("Shade")
            }
            R.id.option_tint -> {
              currentSlider = SliderType.TINT
              sliderMenuTextView.setText("Tint")
            }
            else -> return false
          }

          initBarLine()
          return true
        }
      })

      popup.show()
    }
  }

  private fun initSeekBar() {
    seekBar = findViewById(R.id.seek_bar)

    setSeekBarPosition()

    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onStartTrackingTouch(seekBar: SeekBar?) { /* Required empty */ }
      override fun onStopTrackingTouch(seekBar: SeekBar?) { /* Required empty */ }

      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        when (currentSlider) {
          SliderType.COLOR -> colorRatio = progress.toDouble() / seekBar.max.toDouble()
          SliderType.SHADE -> shadeRatio = progress.toDouble() / seekBar.max.toDouble()
          SliderType.TINT -> tintRatio = progress.toDouble() / seekBar.max.toDouble()
        }
        onColorChanged()
      }
    })
  }

  private fun initBarLine() {
    val gradientDrawable = when (currentSlider) {
      SliderType.COLOR -> {
        GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(
          Color.parseColor("#FF0000"),
          Color.parseColor("#FFFF00"),
          Color.parseColor("#00FF00"),
          Color.parseColor("#00FFFF"),
          Color.parseColor("#0000FF"),
          Color.parseColor("#FF00FF"),
          Color.parseColor("#FF0000")
        ))
      }

      SliderType.SHADE -> {
        GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(
          Color.parseColor(String.format("#%06X", 0xFFFFFF and getTintedColor(getPureColor()))),
          Color.parseColor("#000000")
        ))
      }

      SliderType.TINT -> {
        GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(
          Color.parseColor(String.format("#%06X", 0xFFFFFF and getCurrentColor())),
          Color.parseColor(String.format("#%06X", 0xFFFFFF and getTintedColor(getShadedColor(getPureColor()), 1.0)))
        ))
      }
    }

    seekBarLine = findViewById(R.id.seek_bar_line)
    seekBarLine.background = gradientDrawable
    setSeekBarPosition()
  }

  private fun initPreview() {
    preview = findViewById(R.id.preview)
    preview.setColorFilter(getCurrentColorHex())
  }



  // ------------------------ COLOR GETTERS ------------------------

  private fun getIthColor(i: Int): Int {
    val numberOfColors = 16777216.0
    val segmentWidth = numberOfColors / 6.0
    val spanNumber = floor(i.toDouble() / segmentWidth)
    val progressInSegment = (i.toDouble() - (spanNumber * segmentWidth)) / (segmentWidth)

    val full = 255
    val fadeIn = (255.0 * progressInSegment).roundToInt()
    val fadeOut = (255.0 * (1.0 - progressInSegment)).roundToInt()
    val none = 0

    return when {
      spanNumber < 1 -> Color.argb(full, full, fadeIn, none)
      spanNumber < 2 -> Color.argb(full, fadeOut, full, none)
      spanNumber < 3 -> Color.argb(full, none, full, fadeIn)
      spanNumber < 4 -> Color.argb(full, none, fadeOut, full)
      spanNumber < 5 -> Color.argb(full, fadeIn, none, full)
      spanNumber <= 6 -> {
        if (i != numberOfColors.toInt()) Color.argb(full, full, none, fadeOut)
        else Color.argb(full, full, none, 1)
      }
      else -> Color.argb(full, none, none, none)
    }
  }

  private fun getPureColor(): Int {
    return getIthColor((seekBar.max.toDouble() * colorRatio).roundToInt())
  }

  private fun getShadedColor(color: Int, shadeFactor: Double = 1.0 - shadeRatio): Int {
    val red = (Color.red(color) * shadeFactor).roundToInt()
    val green = (Color.green(color) * shadeFactor).roundToInt()
    val blue = (Color.blue(color) * shadeFactor).roundToInt()

    return Color.argb(255, red, green, blue)
  }

  private fun getTintedColor(color: Int, tintRatio: Double = this.tintRatio): Int {
    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)

    return when (max(red, max(green, blue))) {
      red -> {
        Color.argb(255,
          red,
          green + ((red - green).toFloat() * tintRatio).roundToInt(),
          blue + ((red - blue).toFloat() * tintRatio).roundToInt())
      }

      green -> {
        Color.argb(255,
          red + ((green - red).toFloat() * tintRatio).roundToInt(),
          green,
          blue + ((green - blue).toFloat() * tintRatio).roundToInt())
      }

      blue -> {
        Color.argb(255,
          red + ((blue - red).toFloat() * tintRatio).roundToInt(),
          green + ((blue - green).toFloat() * tintRatio).roundToInt(),
          blue)
      }

      else -> {
        Log.d(TAG, "getCurrentColor: unable to tint")
        Color.argb(255, red, green, blue)
      }
    }
  }

  private fun onColorChanged() {
    val hexColor = getCurrentColorHex()
    preview.setColorFilter(hexColor)
    listener?.onColorChanged(hexColor)
  }



  // ------------------------ PUBLIC FUNCTIONS ------------------------

  fun getCurrentColor(): Int {
    return getTintedColor(getShadedColor(getPureColor()))
  }

  fun getCurrentColorHex(): Int {
    val hexColor = String.format("#%06X", 0xFFFFFF and getCurrentColor())
    return Color.parseColor(hexColor)
  }

  fun setOnColorSelectedListener(listener: ColorChangedListener) {
    this.listener = listener
  }



  // ------------------------ HELPERS ------------------------

  private fun dpToPx(dp: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).roundToInt()
  }

  private fun setSeekBarPosition() {
    seekBar.progress = when (currentSlider) {
      SliderType.COLOR -> (colorRatio * seekBar.max).roundToInt()
      SliderType.SHADE -> (shadeRatio * seekBar.max).roundToInt()
      SliderType.TINT -> (tintRatio * seekBar.max).roundToInt()
    }
  }



  // ------------------------ INNER CLASSES ------------------------

  interface ColorChangedListener {
    fun onColorChanged(hexColor: Int)
  }

  companion object {
    private const val TAG = "ColorSliderView"
  }

  private enum class SliderType {
    COLOR, SHADE, TINT
  }
}