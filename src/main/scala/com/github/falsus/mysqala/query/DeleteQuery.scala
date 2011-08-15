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

    override def build(values: ListBuffer[Any]): String = {
      "DELETE FROM " +
        firstFromTable.toRawQuery(values) +
        buildWhere(values) +
        buildOrder(values) +
        buildLimit(values)
    }

    override def executeUpdate() = {
      var values = ListBuffer[Any]()

      using(conn.prepareStatement(build(values))) { stmt =>
        setValues(stmt, values, 1)
        stmt.executeUpdate()
      }
    }
  }
}
