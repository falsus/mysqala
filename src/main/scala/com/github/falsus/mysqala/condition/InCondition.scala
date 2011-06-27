package com.github.falsus.mysqala

import selectable.Column

package condition {
  import scala.collection.mutable.ListBuffer

  class InCondition[A, B](col1: Column[A, B], values: Seq[B]) extends Condition {
    override def toRawQueryChild(builder: StringBuilder, values: ListBuffer[Any]) {
      if (values.length == 0) {
        return
      }

      builder.append(col1.toRawQuery)
      builder.append(" IN(")

      var first = true

      for (value <- values) {
        if (first) {
          first = false
        } else {
          builder.append(", ")
        }

        builder.append(value)
      }

      builder.delete(builder.length - 2, builder.length)
      builder.append(")")
    }
  }
}
