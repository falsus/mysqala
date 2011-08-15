package com.github.falsus.mysqala

import selectable.Column

package condition {
  import scala.collection.mutable.ListBuffer

  class InequalityCondition[A, B](greater: Boolean, col: Column[A, B], value: B) extends Condition {
    override def toRawQueryChild(values: ListBuffer[Any]): String = {
      values += value
      col.toRawQuery + " " + (if (greater) ">" else "<") + " ?"
    }
  }

  class LessThanCondition[A, B](col: Column[A, B], value: B) extends InequalityCondition(false, col, value)
  class GreaterThanCondition[A, B](col: Column[A, B], value: B) extends InequalityCondition(true, col, value)
}
