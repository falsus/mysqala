package com.github.falsus.mysqala

import selectable.Column

package condition {
  import scala.collection.mutable.ListBuffer

  class EqualValueCondition[A, B](equal: Boolean, val col: Column[A, B], val value: B) extends Condition {
    override def toRawQueryChild(values: ListBuffer[Any]): String = {
      col.toRawQuery +
        (if (value != null) {
          values += value
          if (equal) " = ?" else " != ?"
        } else {
          if (equal) " IS NULL" else " IS NOT NULL"
        })
    }
  }

  class SameValueCondition[A, B](col: Column[A, B], value: B) extends EqualValueCondition(true, col, value)
  class NotSameValueCondition[A, B](col: Column[A, B], value: B) extends EqualValueCondition(false, col, value)
}
