package com.github.falsus.mysqala

import condition.Condition
import selectable.{ Selectable, Column, OrderedColumn }

package query {
  import scala.collection.mutable.ListBuffer

  trait Query {
    def executeUpdate(): Int = 0
    def build(rawQuery: StringBuilder, values: ListBuffer[Any]): Unit
  }
}
