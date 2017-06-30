package com.github.falsus.mysqala

import com.github.falsus.mysqala.condition._
import com.github.falsus.mysqala.table.Table

package selectable {

  import java.sql.ResultSet

  class LongColumn[A](parent: Table[A], propertyName: String, databaseName: String, propertyType: Class[_], columnType: Class[_]) extends Column[A, Long](parent, propertyName, databaseName, propertyType, columnType) {
    def ==(value: Option[Long]): SameValueCondition[A, Long] = new SameValueCondition(this, value)

    def !=(value: Option[Long]): NotSameValueCondition[A, Long] = new NotSameValueCondition(this, value)

    def in(value: Long*): InCondition[A, Long] = new InCondition(this, value)

    def IN(value: Long*) = in(value: _*)

    def >(value: Long): LessThanCondition[A, Long] = new LessThanCondition(this, value)

    def <(value: Long): GreaterThanCondition[A, Long] = new GreaterThanCondition(this, value)

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
