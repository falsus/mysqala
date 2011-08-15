package com.github.falsus.mysqala.selectable

class WildCard extends Selectable {
  def toRawQuery = "*"
}
