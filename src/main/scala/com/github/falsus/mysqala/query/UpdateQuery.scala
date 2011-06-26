package com.github.falsus.mysqala

import connection.ConnectionManager
import util.Using
import table.Table
import condition.{ SameValueCondition }

package query {
  import scala.collection.mutable.ListBuffer

  class UpdateQuery(val connManager: ConnectionManager, tables_ : Table[_]*) extends WhereQuery[UpdateQuery] with Using {
    private def conn = connManager.connection
    
    val tables = tables_.init
    val subInstance = this
    var setters: Seq[SameValueCondition[_, _]] = null

    from(tables_.last)

    def set(sets: SameValueCondition[_, _]*) = {
      setters = sets
      this
    }

    def SET(sets: SameValueCondition[_, _]*) = set(sets: _*)

    override def build(rawQuery: StringBuilder, values: ListBuffer[Any]) = {
      rawQuery.append("UPDATE ")

      for (table <- tables) {
        rawQuery.append(table.toRawQuery)
        rawQuery.append(", ")
      }

      firstFromTable.toRawQuery(rawQuery, values)

      rawQuery.append(" SET ")

      var first = true

      for (setter <- setters) {
        if (first) {
          first = false
        } else {
          rawQuery.append(", ")
        }

        setter.toRawQuery(rawQuery, values)
      }

      buildWhere(rawQuery, values)
      buildOrder(rawQuery, values)
      buildLimit(rawQuery, values)
    }

    override def executeUpdate(): Int = {
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      build(rawQuery, values)

      using(conn.prepareStatement(rawQuery.toString)) { stmt =>
        var index = 1
        for (value <- values) {
          value match {
            case num: Int => stmt.setInt(index, num)
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
