package com.github.falsus.mysqala

import table.Table
import condition.{ SameValueCondition, NotSameValueCondition, InCondition, LowerCondition, UpperCondition }

package selectable {
  import java.sql.ResultSet

  class LongColumn[A](parent: Table[A], propertyName: String, databaseName: String, propertyType: Class[_], columnType: Class[_]) extends Column[A, Long](parent, propertyName, databaseName, propertyType, columnType) {
    def ==(value: Long): SameValueCondition[A, Long] = {
      new SameValueCondition(this, value)
    }

    def !=(value: Long): NotSameValueCondition[A, Long] = {
      new NotSameValueCondition(this, value)
    }

    def in(value: Long*): InCondition[A, Long] = {
      new InCondition(this, value: _*)
    }

    def >(value: Long): LowerCondition[A, Long] = {
      new LowerCondition(this, value)
    }

    def <(value: Long): UpperCondition[A, Long] = {
      new UpperCondition(this, value)
    }

    override def toField(rs: ResultSet, index: Int): AnyRef = {
      if (propertyType == classOf[Option[_]]) {
        val long = rs.getLong(index)

        if (rs.wasNull) None else Some(long)
      } else {
        new java.lang.Long(rs.getLong(index))
      }
    }
  }
}
