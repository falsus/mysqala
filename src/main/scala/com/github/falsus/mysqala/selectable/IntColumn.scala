package com.github.falsus.mysqala

import com.github.falsus.mysqala.condition._
import com.github.falsus.mysqala.table.Table

package selectable {

  import java.sql.ResultSet

  class IntColumn[A](parent: Table[A], propertyName: String, databaseName: String, propertyType: Class[_], columnType: Class[_]) extends Column[A, Int](parent, propertyName, databaseName, propertyType, columnType) {
    def ==(value: Int): SameValueCondition[A, Int] = new SameValueCondition(this, value)

    def !=(value: Int): NotSameValueCondition[A, Int] = new NotSameValueCondition(this, value)

    def in(value: Int*): InCondition[A, Int] = new InCondition(this, value)

    def IN(value: Int*) = in(value: _*)

    def >(value: Int): LessThanCondition[A, Int] = new LessThanCondition(this, value)

    def <(value: Int): GreaterThanCondition[A, Int] = new GreaterThanCondition(this, value)

    override def toField(rs: ResultSet, index: Int): AnyRef = {
      if (propertyType == classOf[Option[_]]) {
        val int = rs.getInt(index)

        if (rs.wasNull) None else Some(int)
      } else {
        new Integer(rs.getInt(index))
      }
    }
  }

}
