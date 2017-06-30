package com.github.falsus.mysqala

import com.github.falsus.mysqala.selectable.Column

package condition {

  import scala.collection.mutable.ListBuffer

  class EqualColumnCondition[A, B, C, D](equal: Boolean, col1: Column[A, B], col2: Column[C, D]) extends Condition {
    override def toRawQueryChild(values: ListBuffer[Any]): String = {
      col1.toRawQuery + (if (equal) " = " else " != ") + col2.toRawQuery
    }
  }

  class SameColumnCondition[A, B, C, D](col1: Column[A, B], col2: Column[C, D]) extends EqualColumnCondition(true, col1, col2)

  class NotSameColumnCondition[A, B, C, D](col1: Column[A, B], col2: Column[C, D]) extends EqualColumnCondition(false, col1, col2)

}
