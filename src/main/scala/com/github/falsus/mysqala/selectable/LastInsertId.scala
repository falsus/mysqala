package com.github.falsus.mysqala.selectable

class LastInsertId extends Selectable {
  def toRawQuery = {
    "LAST_INSERT_ID()"
  }
}
