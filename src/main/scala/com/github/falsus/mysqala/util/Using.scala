package com.github.falsus.mysqala.util

trait Using {
  def using[A <: { def close() }, B](resource: A)(proc: A => B): B = {
    try {
      proc(resource)
    } finally {
      if (resource != null) {
        resource.close()
      }
    }
  }
}
