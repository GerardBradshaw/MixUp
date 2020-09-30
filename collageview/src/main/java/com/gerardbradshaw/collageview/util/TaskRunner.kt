package com.gerardbradshaw.collageview.util

import java.util.*

class TaskRunner {

  private val runnables: LinkedList<Runnable> = LinkedList()
  private var isTaskRunning = false

  fun addNewTask(runnable: Runnable) {
    runnables.add(runnable)
    executeNextTask()
  }

  private fun executeNextTask() {
    if (!isTaskRunning && runnables.isNotEmpty()) {
      isTaskRunning = true
      runnables.poll()?.run()
    }
  }

  fun setTaskFinished() {
    isTaskRunning = false
    executeNextTask()
  }
}