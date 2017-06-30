package com.github.falsus.mysqala

import com.github.falsus.mysqala.selectable.Column

package condition {

  import scala.collection.mutable.ListBuffer

  class EqualValueCondition[A, B](equal: Boolean, val col: Column[A, B], val value: Option[B]) extends Condition {
    override def toRawQueryChild(values: ListBuffer[Any]): String = {
      col.toRawQuery +
        value.map { v =>
          values += v
          if (equal) " = ?" else " != ?"
        }.getOrElse {
          if (equal) " IS NULL" else " IS NOT NULL"
        }
    }
  }

  class SameValueCondition[A, B](col: Column[A, B], value: Option[B]) extends EqualValueCondition(true, col, value)

  class NotSameValueCondition[A, B](col: Column[A, B], value: Option[B]) extends EqualValueCondition(false, col, value)

}
