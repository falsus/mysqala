package com.github.falsus.mysqala

import selectable.Selectable
import util.Using
import connection.ConnectionManager

package query {
  import scala.collection.mutable.ListBuffer

  class SelectLastInsertIdQuery(connManager: ConnectionManager, colsArray: Selectable*) extends SelectQuery(None, connManager, colsArray: _*) with Using {
    override def build(rawQuery: StringBuilder, values: ListBuffer[Any]): Unit = {
      rawQuery.append("SELECT LAST_INSERT_ID()")
    }
    
    private def conn = connManager.connection

    def get: Int = {
      var rawQuery = new StringBuilder()

      using(conn.prepareStatement(rawQuery.toString)) { stmt =>
        using(stmt.executeQuery()) { rs =>
          return rs.getInt(1)
        }
      }
    }
  }
}
