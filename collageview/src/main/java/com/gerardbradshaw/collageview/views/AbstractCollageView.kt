package com.gerardbradshaw.collageview.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.gerardbradshaw.collageview.R
import com.gerardbradshaw.collageview.util.ImageParams
import com.gerardbradshaw.collageview.util.TaskRunner
import com.ortiz.touchview.TouchImageView
import java.lang.IndexOutOfBoundsException
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * This class should be inherited in order to reduce code duplication in CollageView classes. Some
 * properties and functions are only used by the inheriting class. Note the inheriting class should
 * also implement [View.OnTouchListener] and call [onTouchHelper]. */
abstract class AbstractCollageView(
  context: Context,
  attrs: AttributeSet?,
  private val imageCount: Int,
  var layoutWidth: Int = 0,
  var layoutHeight: Int = 0,
  var isBorderEnabled: Boolean = false,
  protected var imageUris: Array<Uri?>? = null
) : FrameLayout(context, attrs), CollageView {

  // ------------------------ COLLAGE VIEW INSTANCE PROPERTIES ------------------------

  /** True if the layout in the inheriting class has been inflated. */
  protected var isLayoutInflated = false

  /** A list of TouchImageViews in the inheriting class. */
  protected val imageViews: List<TouchImageView> = List(imageCount) {
    val image = TouchImageView(context, null).also {
      it.setBackgroundColor(Color.WHITE)
      it.layoutParams = LayoutParams(0, 0)
    }
    image
  }

  /** The height, width, x and y positions of each image in the view. */
  protected var imageSizeAndPosCache = Array(imageCount) {
    ImageParams()
  }

  protected val minDimension = 100f


  // ------------------------ ABSTRACT COLLAGE VIEW PROPERTIES ------------------------

  private val initialLayoutWidth: Int = layoutWidth
  private val initialLayoutHeight: Int = layoutHeight

  /** True if an ACTION_DOWN motion event is detected without an ACTION_MOVE or ACTION_UP within the
  time specified by LONG_CLICK_DURATION. */
  private var isLongClickActive = false

  /** The time at which the last ACTION_DOWN motion event was detected. */
  private var touchStartTime: Long? = null
  
  /** The raw x and y coordinates of the last ACTION_DOWN motion event */
  private var initialTouchRawX = 0f
  private var initialTouchRawY = 0f

  /** The raw x and y coordinates of the last motion event. */
  private var touchRawX = 0f
  private var touchRawY = 0f

  /** The index and [Edge] of the image touched during the last ACTION_DOWN touch event. */
  private var touchedImageIndex = -1
  protected var touchedImageEdge: Edge? = null

  /** The class listening for clicks on TouchImageViews */
  private var clickListener: OnClickListener? = null

  /** The TaskRunner used to ensure the previous resize event has finished before the next is
   started. */
  private val resizeTaskRunner = TaskRunner()

  private var borderColor = 0

  /** Returns the number if TouchImageViews in the View. */
  override fun imageCount() = imageCount

  // ------------------------ INITIALIZATION (ABSTRACT AND INSTANCE) ------------------------

  init {
    initLayoutParams(layoutWidth, layoutHeight)
  }

  private fun initLayoutParams(width: Int, height: Int) {
    if (layoutParams == null) {
      layoutParams = LayoutParams(width, height)
    }
    else {
      layoutParams.width = width
      layoutParams.height = height
    }
  }

  /** Adds empty TouchImageViews to the layout. */
  protected fun addViewsToLayout() {
    for (image in imageViews) {
      addView(image)
    }
  }

  /** Adds images from [imageUris] to the TouchImageViews in the layout. */
  protected fun addImagesToViews() {
    for (i in 0 until imageCount) {
      setImageAt(i, imageUris?.get(i))
    }
  }



  // ------------------------  IMAGES ------------------------

  /** Sets the image at child [index] to the image at [uri]. If Uri is invalid, the default image is
   loaded. */
  override fun setImageAt(index: Int, uri: Uri?) {
    when {
      index >= imageViews.size -> Log.d(TAG, "setImageAt: invalid index")
      uri == null -> loadDefaultImageInView(index)
      else -> loadImageInView(index, uri)
    }
  }

  private fun loadDefaultImageInView(index: Int) {
    imageViews[index].apply {
      scaleType = ImageView.ScaleType.CENTER_INSIDE


      Glide.with(context)
        .load(R.drawable.img_tap_to_add_photo)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)

      isZoomEnabled = false
      minZoom = 1f
      setZoom(this.minZoom)

      setTag(R.id.image_source, context.getString(R.string.default_image_tag))
    }
  }

  private fun loadImageInView(index: Int, uri: Uri) {
    imageViews[index].apply {
      scaleType = ImageView.ScaleType.CENTER

      Glide.with(context)
        .load(uri)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)

      isZoomEnabled = true
      minZoom = getMinZoom(index)
      setZoom(minZoom)

      setTag(R.id.image_source, uri.toString())
    }
  }

  /** Sets the listener to be notified of image clicks. The View given in the OnClick() interface
  is the TouchImageView clicked and not this (parent) CollageView. */
  fun setImageClickListener(listener: OnClickListener) {
    clickListener = listener
    for (image in imageViews) image.setOnClickListener(listener)
  }

  fun getSetImageCount(): Int {
    var count = 0

    for (view in imageViews) {
      if (view.getTag(R.id.image_source) != context.getString(R.string.default_image_tag)) count++
    }

    return count
  }

  fun isSetImageAt(position: Int): Boolean {
    if (position < imageCount) {
      return imageViews[position].getTag(R.id.image_source) != context.getString(R.string.default_image_tag)
    }
    else throw IndexOutOfBoundsException("position index too large")
  }



  // ------------------------  ASPECT RATIO ------------------------

  /** The last aspect ratio set (null if no specific ratio has been set).  */
  var aspectRatio: Float? = null
    set(value) {
      when {
        !isLayoutInflated -> {
          Log.d(TAG, "setAspectRatio: Error - collage not inflated.")
          return
        }

        value == null -> {
          resizeLayout(initialLayoutWidth, initialLayoutHeight)
        }

        else -> {
          val shouldAdjustHeight = initialLayoutHeight > initialLayoutWidth / value

          if (shouldAdjustHeight) {
            resizeLayout(initialLayoutWidth, (initialLayoutWidth.toFloat() / value).toInt())
          } else {
            resizeLayout((initialLayoutHeight.toFloat() * value).toInt(), initialLayoutHeight)
          }
        }
      }
      field = value
    }

  private fun resizeLayout(newWidth: Int, newHeight: Int) {
    initLayoutParams(newWidth, newHeight)

    val currentXScale = layoutWidth.toFloat() / initialLayoutWidth.toFloat()
    val newXScale = newWidth / initialLayoutWidth.toFloat()

    val currentYScale = layoutHeight.toFloat() / initialLayoutHeight.toFloat()
    val newYScale = newHeight / initialLayoutHeight.toFloat()

    layoutWidth = newWidth
    layoutHeight = newHeight

    scaleImageViews(newXScale / currentXScale, newYScale / currentYScale)
  }

  private fun scaleImageViews(xScale: Float, yScale: Float) {
    for (params in imageSizeAndPosCache) {
      params.width *= xScale
      params.height *= yScale
      params.x *= xScale
      params.y *= yScale
    }
    syncViewsWithSizeAndPosCache()
  }

  protected fun syncViewsWithSizeAndPosCache() {
    for (i in imageViews.indices) {
      if (!imageSizeAndPosCache[i].synced) {
        syncViewSizeWithCacheAt(i)
        syncViewPositionWithCacheAt(i)
        imageSizeAndPosCache[i].synced = true
      }
    }
  }

  private fun syncViewSizeWithCacheAt(index: Int) {
    if (index < 0 || index >= imageCount) return

    val image = getChildAt(index) as TouchImageView
    val newDimens = imageSizeAndPosCache[index]

    if (image.layoutParams != null) {
      image.updateLayoutParams {
        if (newDimens.width != 0f) this.width = newDimens.width.roundToInt()
        if (newDimens.height != 0f) this.height = newDimens.height.roundToInt()
      }
    }

    image.minZoom = getMinZoom(index)
    fitImageInView(index)
  }

  private fun getMinZoom(position: Int): Float {
    val drawable = imageViews[position].drawable

    if (drawable != null) {
      val drawableWidth = drawable.intrinsicWidth
      val drawableHeight = drawable.intrinsicHeight

      if (drawableWidth > 0 && drawableHeight > 0) {
        val sizeAndPos = imageSizeAndPosCache[position]

        val widthRatio = sizeAndPos.width / drawableWidth
        val heightRatio = sizeAndPos.height / drawableHeight
        return max(widthRatio, heightRatio)
      }
    }

    return 1f
  }

  private fun fitImageInView(index: Int) {
    val image = imageViews[index]
    if (image.currentZoom < image.minZoom &&
      image.getTag(R.id.image_source) != context.getString(R.string.default_image_tag)
    ) {
      image.setZoom(image.minZoom)
    }
  }

  private fun syncViewPositionWithCacheAt(index: Int) {
    if (index < 0 || index >= imageCount) return

    val image = imageViews[index]

    image.x = imageSizeAndPosCache[index].x.roundToInt().toFloat()
    image.y = imageSizeAndPosCache[index].y.roundToInt().toFloat()
  }

  private fun addResizeRunnableToQueueAndRun(
    event: MotionEvent,
    touchRawX: Float,
    touchRawY: Float,
    resizeImage: (index: Int, deltaX: Float, deltaY: Float) -> Unit
  ) {
    resizeTaskRunner.addNewTask(Runnable {
      val deltaX = event.rawX - touchRawX
      val deltaY = event.rawY - touchRawY

      resizeImage(touchedImageIndex, deltaX, deltaY)

      fitImageInView(touchedImageIndex)

      resizeTaskRunner.setTaskFinished()
    })
  }



  // ------------------------  BORDER ------------------------

  /** Enables the border if [enableBorder] is true, otherwise the border is disabled. */
  @SuppressLint("NewApi") // bug
  fun enableBorder(enableBorder: Boolean) {
    isBorderEnabled = enableBorder

    if (isBorderEnabled) {
      val frameBorder = ContextCompat.getDrawable(context, R.drawable.border_frame)
      val frameBorderThickness =
        TypedValue.applyDimension(
          TypedValue.COMPLEX_UNIT_DIP,
          resources.getDimension(R.dimen.collage_layout_border_thickness),
          resources.displayMetrics
        ).roundToInt()

      (frameBorder?.mutate() as GradientDrawable)
        .setStroke(2 * frameBorderThickness, borderColor)

      foreground = frameBorder

      for (image in imageViews) {
        val imageBorder = ContextCompat.getDrawable(context, R.drawable.border_image)

        (imageBorder?.mutate() as GradientDrawable)
          .setStroke(frameBorderThickness, borderColor)

        image.foreground = imageBorder
        image.setTag(R.id.border_color, borderColor)
      }
    }
    else {
      foreground = null

      for (image in imageViews) {
        image.foreground = null
      }
    }
  }

  /** Toggles the border on or off depending on its current state. */
  fun toggleBorder() {
    isBorderEnabled = !isBorderEnabled
    enableBorder(isBorderEnabled)
  }

  fun setBorderColor(color: Int) {
    borderColor = color

    if (isLayoutInflated && isBorderEnabled) {
      enableBorder(true)
    }
  }

  /** Returns the current border color, or null if border is not enabled. */
  fun getBorderColor(): Int? {
    return if (isLayoutInflated && isBorderEnabled) borderColor else null
  }



  // ------------------------  UTIL ------------------------

  fun reset() {
    for (i in 0 until imageCount) {
      setImageAt(i, null)
    }

    aspectRatio = initialLayoutWidth.toFloat() / initialLayoutHeight.toFloat()
    enableBorder(false)
    initImageLayout()
  }

  /** Helper for the inheriting class' onTouch() method. This reduced the need to duplicate the
  below code in each class inheriting from AbstractCollageView. */
  protected fun onTouchHelper(
    view: View,
    event: MotionEvent,
    resizeImage: (index: Int, deltaX: Float, deltaY: Float) -> Unit
  ): Boolean {
    return when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        saveTouchEventProperties(view, event)
        if (touchedImageIndex == -1) touchStartTime = null
        super.onTouchEvent(event)
      }

      MotionEvent.ACTION_MOVE -> {
        if (!isLongClickActive) {
          if (touchStartTime == null) return super.onTouchEvent(event)

          val isLongClick =
            Calendar.getInstance().timeInMillis - touchStartTime!! > LONG_CLICK_DURATION

          if (!isLongClick) {
            val isPseudoMove = event.rawX == initialTouchRawX && event.rawY == initialTouchRawY

            return if (!isPseudoMove) {
              resetTouchEventProperties()
              super.onTouchEvent(event)
            }
            else false
          }
          isLongClickActive = true
        }

        addResizeRunnableToQueueAndRun(event, touchRawX, touchRawY, resizeImage)
        touchRawX = event.rawX
        touchRawY = event.rawY
        true
      }

      MotionEvent.ACTION_UP -> {
        if (!isLongClickActive) {
          resetTouchEventProperties()
          return super.onTouchEvent(event)
        }

        addResizeRunnableToQueueAndRun(event, touchRawX, touchRawY, resizeImage)
        resetTouchEventProperties()
        true
      }

      else -> super.onTouchEvent(event)
    }
  }

  /** Returns an [Edge] representing the edge or corner at the coordinates (touchX, touchY) in the
   image at index touchedImageIndex in [imageViews]. */
  private fun getTouchedImageEdge(touchX: Float, touchY: Float): Edge? {
    if (touchedImageIndex == -1 || touchedImageIndex >= imageCount) {
      Log.d(TAG, "determineEdge: invalid image index")
      return null
    }

    val percentageAcrossImage = touchX / imageSizeAndPosCache[touchedImageIndex].width
    val percentageDownImage = touchY / imageSizeAndPosCache[touchedImageIndex].height
    
    return when {
      percentageAcrossImage < 0.25 -> {
        when {
          percentageDownImage < 0.25 -> Edge.TOP_LEFT_CORNER
          percentageDownImage < 0.75 -> Edge.LEFT_SIDE
          else -> Edge.BOTTOM_LEFT_CORNER
        }
      }
      percentageAcrossImage < 0.75 -> {
        when {
          percentageDownImage < 0.25 -> Edge.TOP_SIDE
          percentageDownImage > 0.75 -> Edge.BOTTOM_SIDE
          else -> null
        }
      }
      percentageAcrossImage > 0.75 -> {
        when {
          percentageDownImage < 0.25 -> Edge.TOP_RIGHT_CORNER
          percentageDownImage < 0.75 -> Edge.RIGHT_SIDE
          else -> Edge.BOTTOM_RIGHT_CORNER
        }
      }
      else -> {
        Log.d(TAG, "determineEdge: something went wrong")
        null
      }
    }
  }

  private fun saveTouchEventProperties(view: View, event: MotionEvent) {
    isLongClickActive = false
    touchStartTime = Calendar.getInstance().timeInMillis

    initialTouchRawX = event.rawX
    initialTouchRawY = event.rawY
    touchRawX = initialTouchRawX
    touchRawY = initialTouchRawY

    touchedImageIndex = imageViews.indexOf(view)
    touchedImageEdge = getTouchedImageEdge(event.x, event.y)
  }

  private fun resetTouchEventProperties() {
    isLongClickActive = false
    touchStartTime = null

    initialTouchRawX = 0f
    initialTouchRawY = 0f
    touchRawX = 0f
    touchRawY = 0f

    touchedImageEdge = null
    touchedImageIndex = -1
  }

  protected abstract fun initImageLayout()



  // ------------------------ HELPERS ------------------------

  /** Enum class used to determine which [Edge] of the image  was touched */
  protected enum class Edge {
    TOP_LEFT_CORNER,
    TOP_RIGHT_CORNER,
    BOTTOM_LEFT_CORNER,
    BOTTOM_RIGHT_CORNER,
    TOP_SIDE,
    BOTTOM_SIDE,
    LEFT_SIDE,
    RIGHT_SIDE
  }

  companion object {
    private const val TAG = "AbstractCollageView"
    private const val LONG_CLICK_DURATION = 300L
  }
}