package com.github.falsus.mysqala

import selectable.Column

package condition {
  import scala.collection.mutable.ListBuffer

  class SameValueCondition[A, B](val col: Column[A, B], val value: B) extends Condition {
    override def toRawQueryChild(builder: StringBuilder, values: ListBuffer[Any]) {
      builder.append(col.toRawQuery)

      if (value != null) {
        builder.append(" = ?")
        values += value
      } else {
        builder.append(" IS NULL")
      }
    }
  }
}
