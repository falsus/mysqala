package com.github.falsus.mysqala

import selectable.Column

package condition {
  import scala.collection.mutable.ListBuffer

  class SameColumnCondition[A, B, C, D](col1: Column[A, B], col2: Column[C, D]) extends Condition {
    override def toRawQueryChild(builder: StringBuilder, values: ListBuffer[Any]) {
      builder.append(col1.toRawQuery)
      builder.append(" = ")
      builder.append(col2.toRawQuery)
    }
  }
}
