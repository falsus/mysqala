package com.github.falsus.mysqala

import connection.ConnectionManager
import util.Using
import condition.Condition
import table.Table

package query {
  import scala.collection.mutable.ListBuffer

  class DeleteQuery(val connManager: ConnectionManager) extends WhereQuery[DeleteQuery] with Using {
    private def conn = connManager.connection

    val subInstance = this

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
        var index = 1
        for (value <- values) {
          value match {
            case num: Int => stmt.setInt(index, num)
            case num: Long => stmt.setLong(index, num)
            case text: String => stmt.setString(index, text)
            case date: java.util.Date => stmt.setTimestamp(index, new java.sql.Timestamp(date.getTime))
            case _ => println("atode reigai")
          }

          index += 1
        }

        stmt.executeUpdate()
      }
    }
  }
}
