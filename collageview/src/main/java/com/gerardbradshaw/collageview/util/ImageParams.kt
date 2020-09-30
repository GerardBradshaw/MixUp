package com.gerardbradshaw.collageview.util

class ImageParams(width: Float = 0f, height: Float = 0f, x: Float = 0f, y: Float = 0f) {

  var width: Float = width
    set(value) {
      field = value
      synced = false
    }

  var height: Float = height
    set(value) {
      field = value
      synced = false
    }

  var x: Float = x
    set(value) {
      field = value
      synced = false
    }

  var y: Float = y
    set(value) {
      field = value
      synced = false
    }

  var synced = false

}