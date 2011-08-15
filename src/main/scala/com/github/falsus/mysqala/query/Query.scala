package com.github.falsus.mysqala

import condition.Condition
import selectable.{ Selectable, Column, OrderedColumn }

package query {
  import scala.collection.mutable.ListBuffer

  trait Query {
    def executeUpdate(): Int = 0
    def build(values: ListBuffer[Any]): String

    def setValues(stmt: java.sql.PreparedStatement, values: Seq[_], index: Int): Int = {
      var currentIndex = index

      for (value <- values) {
        value match {
          case num: Int => stmt.setInt(currentIndex, num)
          case text: String => stmt.setString(currentIndex, text)
          case date: java.util.Date => stmt.setTimestamp(currentIndex, new java.sql.Timestamp(date.getTime))
          case seq: Seq[_] => currentIndex = setValues(stmt, seq, currentIndex) - 1
          case _ => println("atode reigai")
        }

        currentIndex += 1
      }

      currentIndex
    }
  }
}
