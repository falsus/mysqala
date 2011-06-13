package com.github.falsus.mysqala

import selectable.Column

package condition {
  import scala.collection.mutable.ListBuffer

  class LowerCondition[A, B](col: Column[A, B], value: B) extends Condition {
    override def toRawQueryChild(builder: StringBuilder, values: ListBuffer[Any]) {
      builder.append(col.toRawQuery)
      builder.append(" > ?")
      values += value
    }
  }
}
