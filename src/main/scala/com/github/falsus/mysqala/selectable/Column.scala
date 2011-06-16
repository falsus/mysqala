package com.github.falsus.mysqala

import table.Table

package selectable {
  import java.sql.ResultSet

  abstract class Column[A, B](val parent: Table[A], val propertyName: String, val databaseName: String, val propertyType: Class[_], val columnType: Class[_]) extends Selectable {
    override def toRawQuery = {
      parent.shortDatabaseTableName + "." + databaseName
    }

    def toRawQuerySingle = {
      databaseName
    }

    def asc = {
      new OrderedColumn(this, true)
    }

    def desc = {
      new OrderedColumn(this, false)
    }

    def ASC = asc
    def DESC = desc
    
    def toField(rs: ResultSet, index: Int): AnyRef

    override def hashCode() = {
      parent.hashCode + propertyName.hashCode
    }

    override def equals(obj: Any): Boolean = {
      obj match {
        case col: Column[_, _] => col.parent == parent && col.propertyName == propertyName
        case _ => false
      }
    }
  }
}
