package com.github.falsus.mysqala

import com.github.falsus.mysqala.condition.{Condition, PlaceHolder}
import com.github.falsus.mysqala.query.{DeleteQuery, PreInsertQuery, SelectQuery, UpdateQuery}
import com.github.falsus.mysqala.selectable._

package table {

  import scala.collection.mutable.LinkedHashMap

  trait Table[A] {
    def shortDatabaseTableName: String

    def toRawQuery: String

    def toRawQuerySingle: String

    def tableName: String

    def tableClass: Class[A]

    def columns: List[Column[A, _]]

    def select(columns: Selectable*): SelectQuery

    def last_insert_id(): LastInsertId

    def delete: DeleteQuery

    def update(tables: Table[_]*): UpdateQuery

    def insert: PreInsertQuery[A]

    def SELECT(columns: Selectable*) = select(columns: _*)

    def LAST_INSERT_ID() = last_insert_id

    def DELETE = delete

    def UPDATE(tables: Table[_]*) = update(tables: _*)

    def INSERT = insert

    def * : WildCardInTable

    def ? = new PlaceHolder()

    def find(cond: Condition): Option[A]

    def getIntColumn(propertyName: String): IntColumn[A]

    def getLongColumn(propertyName: String): LongColumn[A]

    def getStringColumn(propertyName: String): StringColumn[A]

    def getDateColumn(propertyName: String): DateColumn[A]

    def cloneForInnerJoin: Table[A]

    override def hashCode(): Int = tableName.hashCode

    override def equals(obj: Any): Boolean = {
      obj match {
        case table: Table[_] => tableName == table.tableName
        case _ => false
      }
    }
  }

  object Table {
    val simpleTableNames = LinkedHashMap[String, String]()

    def toSimpleTableName(tableName: String) = {
      val baseName = tableName.substring(0, 1)
      var count = 1
      var name = baseName

      while (simpleTableNames.contains(name)) {
        name = baseName + count
        count += 1
      }

      simpleTableNames.put(name, tableName)
      name
    }
  }

}
