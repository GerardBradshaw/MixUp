package com.gerardbradshaw.collageview.util

import java.util.*

class TaskRunner {
  private val runnableList: LinkedList<Runnable> = LinkedList()
  private var isTaskRunning = false

  fun addNewTask(runnable: Runnable) {
    runnableList.add(runnable)
    executeNextTask()
  }

  private fun executeNextTask() {
    if (!isTaskRunning && runnableList.isNotEmpty()) {
      isTaskRunning = true
      runnableList.poll()?.run()
    }
  }

  fun setTaskFinished() {
    isTaskRunning = false
    executeNextTask()
  }
}