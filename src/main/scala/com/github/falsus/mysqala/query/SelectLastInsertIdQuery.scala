package com.github.falsus.mysqala

import selectable.Selectable
import util.Using

package query {
  import java.sql.Connection
  import scala.collection.mutable.ListBuffer

  class SelectLastInsertIdQuery(conn: Connection, colsArray: Selectable*) extends SelectQuery(None, conn, colsArray: _*) with Using {
    override def build(rawQuery: StringBuilder, values: ListBuffer[Any]): Unit = {
      rawQuery.append("SELECT LAST_INSERT_ID()")
    }

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
