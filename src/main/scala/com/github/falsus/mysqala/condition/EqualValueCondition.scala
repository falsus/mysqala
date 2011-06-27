package com.github.falsus.mysqala

import selectable.Column

package condition {
  import scala.collection.mutable.ListBuffer

  class EqualValueCondition[A, B](equal: Boolean, val col: Column[A, B], val value: B) extends Condition {
    override def toRawQueryChild(builder: StringBuilder, values: ListBuffer[Any]) {
      builder.append(col.toRawQuery)

      if (value != null) {
        if (equal) {
          builder.append(" = ?")
        } else {
          builder.append(" != ?")
        }

        values += value
      } else {
        if (equal) {
          builder.append(" IS NULL")
        } else {
          builder.append(" IS NOT NULL")
        }
      }
    }
  }

  class SameValueCondition[A, B](col: Column[A, B], value: B) extends EqualValueCondition(true, col, value)
  class NotSameValueCondition[A, B](col: Column[A, B], value: B) extends EqualValueCondition(false, col, value)
}
