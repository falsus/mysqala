package com.github.falsus.mysqala

import com.github.falsus.mysqala.selectable.Column

import scala.collection.mutable

package condition {

  import scala.collection.mutable.ListBuffer

  class OrderByCondition(col: Column[_, _]) extends Condition {
    private var columns = mutable.LinkedHashMap[Column[_, _], Boolean]()
    val lastColumn: Column[_, _] = col

    columns += col -> true

    def asc = columns(lastColumn) = true

    def desc = columns(lastColumn) = false

    def +=(column: Column[_, _]): mutable.LinkedHashMap[Column[_, _], Boolean] = columns += column -> true

    override def toRawQueryChild(values: ListBuffer[Any]) = ""
  }

}
