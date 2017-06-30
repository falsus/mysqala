package com.github.falsus.mysqala

import com.github.falsus.mysqala.selectable.Column

package condition {

  import scala.collection.mutable.{LinkedHashMap, ListBuffer}

  class OrderByCondition(col: Column[_, _]) extends Condition {
    var columns = LinkedHashMap[Column[_, _], Boolean]()
    var lastColumn = col

    columns += col -> true

    def asc = columns(lastColumn) = true

    def desc = columns(lastColumn) = false

    def +=(column: Column[_, _]) = columns += column -> true

    override def toRawQueryChild(values: ListBuffer[Any]) = ""
  }

}
