package com.github.falsus.mysqala.selectable

trait Selectable {
  def toRawQuery: String
}
