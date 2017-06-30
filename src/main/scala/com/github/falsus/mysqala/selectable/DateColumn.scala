package com.github.falsus.mysqala

import com.github.falsus.mysqala.condition.{Condition, GreaterThanCondition, LessThanCondition, SameValueCondition}
import com.github.falsus.mysqala.table.Table

package selectable {

  import java.sql.ResultSet

  class DateColumn[A](parent: Table[A], propertyName: String, databaseName: String, propertyType: Class[_], columnType: Class[_]) extends Column[A, java.util.Date](parent, propertyName, databaseName, propertyType, columnType) {
    def ==(value: Option[java.util.Date]): SameValueCondition[A, java.util.Date] = new SameValueCondition(this, value)

    def between(value: java.util.Date): Condition = null

    def BETWEEN(value: java.util.Date) = between(value)

    def >(value: java.util.Date): LessThanCondition[A, java.util.Date] = new LessThanCondition(this, value)

    def <(value: java.util.Date): GreaterThanCondition[A, java.util.Date] = new GreaterThanCondition(this, value)

    override def toField(rs: ResultSet, index: Int): AnyRef = {
      if (propertyType == classOf[Option[_]]) {
        if (rs.getTimestamp(index) != null) Some(rs.getTimestamp(index)) else None
      } else {
        rs.getTimestamp(index)
      }
    }
  }

}
