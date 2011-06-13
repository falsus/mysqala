package com.github.falsus.mysqala

import table.Table
import condition.{ Condition, SameValueCondition, LowerCondition, UpperCondition }

package selectable {
  import java.sql.ResultSet

  class DateColumn[A](parent: Table[A], propertyName: String, databaseName: String, propertyType: Class[_], columnType: Class[_]) extends Column[A, java.util.Date](parent, propertyName, databaseName, propertyType, columnType) {
    def ==(value: java.util.Date): SameValueCondition[A, java.util.Date] = {
      new SameValueCondition(this, value)
    }

    def between(value: java.util.Date): Condition = {
      null
    }

    def >(value: java.util.Date): LowerCondition[A, java.util.Date] = {
      new LowerCondition(this, value)
    }

    def <(value: java.util.Date): UpperCondition[A, java.util.Date] = {
      new UpperCondition(this, value)
    }

    override def toField(rs: ResultSet, index: Int): AnyRef = {
      if (propertyType == classOf[Option[_]]) {
        if (rs.getTimestamp(index) != null) Some(rs.getTimestamp(index)) else None
      } else {
        rs.getTimestamp(index)
      }
    }
  }
}
