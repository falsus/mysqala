package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.selectable.Selectable
import com.github.falsus.mysqala.util.Using

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
