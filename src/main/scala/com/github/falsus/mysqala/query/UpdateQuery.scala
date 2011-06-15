package com.github.falsus.mysqala

import util.Using
import table.Table
import condition.{SameValueCondition}

package query {
  import java.sql.Connection
  import scala.collection.mutable.ListBuffer
  
  class UpdateQuery(val conn: Connection, val tables: Table[_]*) extends Query with Using {
    var setters: Seq[SameValueCondition[_, _]] = null

    def set(sets: SameValueCondition[_, _]*) = {
      setters = sets
      this
    }

    def SET(sets: SameValueCondition[_, _]*) = set(sets: _*)
    
    override def build(rawQuery: StringBuilder, values: ListBuffer[Any]) = {
      val rawQuery = new StringBuilder("UPDATE ")
      var first = true

      for (table <- tables) {
        if (first) {
          first = false
        } else {
          rawQuery.append(", ")
        }

        rawQuery.append(table.toRawQuery)
      }

      rawQuery.append(" SET ")

      first = true

      for (setter <- setters) {
        if (first) {
          first = false
        } else {
          rawQuery.append(", ")
        }

        setter.toRawQuery(rawQuery, values)
      }

      if (firstWhereCondition != null) {
        var first = true

        for ((conditoin, and) <- whereConditions) {
          if (first) {
            rawQuery.append(" WHERE ")
            first = false
          } else if (and) {
            rawQuery.append(" AND ")
          } else {
            rawQuery.append(" OR ")
          }

          rawQuery.append("(")
          conditoin.toRawQuery(rawQuery, values)
          rawQuery.append(")")
        }
      }

      rawQuery.toString
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
