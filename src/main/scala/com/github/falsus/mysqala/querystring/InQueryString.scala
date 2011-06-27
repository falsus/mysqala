package com.github.falsus.mysqala

import selectable.Column

package querystring {
  import scala.collection.mutable.ListBuffer

  class InQueryString(column: Column[_, _]) extends DynamicQueryString {
    def build(rawQuery: StringBuilder, values: ListBuffer[_]) {
      rawQuery.append(column.toRawQuery)

      val value = values.remove(0)

      value match {
        case l: Seq[_] =>
          var first = true

          for (v <- l) {
            if (first) {
              rawQuery.append(" IN(")
              first = false
            } else {
              rawQuery.append(", ")
            }

            rawQuery.append("?")
          }

          rawQuery.append(")")

        case _ => throw new Exception("must be List")
      }
    }
  }
}