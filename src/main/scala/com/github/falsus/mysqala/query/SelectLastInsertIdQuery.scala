package com.github.falsus.mysqala

import selectable.Selectable
import util.Using
import connection.ConnectionManager

package query {
  import scala.collection.mutable.ListBuffer

  class SelectLastInsertIdQuery(connManager: ConnectionManager, colsArray: Selectable*) extends SelectQuery(None, connManager, colsArray: _*) with Using {
    private lazy val staticQuery = "SELECT LAST_INSERT_ID()"
    private def conn = connManager.connection

    override def build(values: ListBuffer[Any]): String = staticQuery

    def get: Int = {
      using(conn.prepareStatement(staticQuery)) { stmt =>
        using(stmt.executeQuery()) { rs => return rs.getInt(1) }
      }
    }
  }
}
