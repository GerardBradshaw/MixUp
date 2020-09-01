package com.gerardbradshaw.mixup.utils

class ImageParams {

  var width: Float = 0f
    set(value) {
      field = value
      synced = false
    }

  var height: Float = 0f
    set(value) {
      field = value
      synced = false
    }

  var x: Float = 0f
    set(value) {
      field = value
      synced = false
    }

  var y: Float = 0f
    set(value) {
      field = value
      synced = false
    }

  var synced = false

  constructor(width: Float = 0f, height: Float = 0f, x: Float = 0f, y: Float = 0f) {
    this.width = width
    this.height = height
    this.x = x
    this.y = y
  }
}