package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.util.Using

package query {

  import scala.collection.mutable.ListBuffer

  class DeleteQuery(val connManager: ConnectionManager) extends WhereQuery[DeleteQuery] with Using {
    private def conn = connManager.connection

    override val subInstance: DeleteQuery = this

    override def build(values: ListBuffer[Any]): String = {
      "DELETE FROM " +
        firstFromTable.toRawQuery(values) +
        buildWhere(values) +
        buildOrder(values) +
        buildLimit(values)
    }

    override def executeUpdate(): Int = {
      val values = ListBuffer[Any]()

      using(conn.prepareStatement(build(values))) { stmt =>
        setValues(stmt, values, 1)
        stmt.executeUpdate()
      }
    }
  }

}
