package com.github.falsus.mysqala

import table.Table
import condition.{ SameValueCondition, NotSameValueCondition, InCondition }

package selectable {
  import java.sql.ResultSet

  class StringColumn[A](parent: Table[A], propertyName: String, databaseName: String, propertyType: Class[_], columnType: Class[_]) extends Column[A, String](parent, propertyName, databaseName, propertyType, columnType) {
    def ==(value: String): SameValueCondition[A, String] = new SameValueCondition(this, value)
    def !=(value: String): NotSameValueCondition[A, String] = new NotSameValueCondition(this, value)
    def in(values: String*): InCondition[A, String] = new InCondition(this, values)
    def IN(values: String*) = in(values: _*)

    override def toField(rs: ResultSet, index: Int): AnyRef = {
      if (propertyType == classOf[Option[_]]) {
        if (rs.getString(index) != null) Some(rs.getString(index)) else None
      } else {
        rs.getString(index)
      }
    }
  }
}
