package com.github.falsus.mysqala

import connection.ConnectionManager
import util.Using
import condition.Condition
import table.Table

package query {
  import scala.collection.mutable.ListBuffer

  class DeleteQuery(val connManager: ConnectionManager) extends WhereQuery[DeleteQuery] with Using {
    private def conn = connManager.connection

    override val subInstance = this

    override def build(rawQuery: StringBuilder, values: ListBuffer[Any]) = {
      rawQuery.append("DELETE FROM ")
      firstFromTable.toRawQuery(rawQuery, values)

      buildWhere(rawQuery, values)
      buildOrder(rawQuery, values)
      buildLimit(rawQuery, values)
    }

    override def executeUpdate() = {
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      build(rawQuery, values)

      using(conn.prepareStatement(rawQuery.toString)) { stmt =>
        setValues(stmt, values, 1)
        stmt.executeUpdate()
      }
    }
  }
}
