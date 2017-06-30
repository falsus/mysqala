package com.github.falsus.mysqala

import com.github.falsus.mysqala.selectable.Column

package condition {

  import scala.collection.mutable.ListBuffer

  class InCondition[A, B](col1: Column[A, B], values: Seq[B]) extends Condition {
    override def toRawQueryChild(values: ListBuffer[Any]): String = {
      if (values.isEmpty) {
        return ""
      }

      col1.toRawQuery + " IN(" + values.mkString(", ") + ")"
    }
  }

}
