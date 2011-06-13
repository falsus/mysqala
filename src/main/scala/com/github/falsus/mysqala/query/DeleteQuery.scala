package com.github.falsus.mysqala

import util.Using
import condition.Condition
import table.Table

package query {
  import java.sql.Connection
  import scala.collection.mutable.ListBuffer

  class DeleteQuery(val conn: Connection) extends Query with Using {
    var firstFromTable: FromTable[_] = null
    var lastJoinTable: FromTable[_] = null

    class FromTable[A](val table: Table[A], val on: Option[Condition] = None) {
      var next: Option[FromTable[_]] = None

      def nextTable = next

      def join[B](joinTable: FromTable[B]) = {
        next = Some(joinTable)
        joinTable
      }

      def toRawQuery(builder: StringBuilder, values: ListBuffer[Any]) {
        if (on != None) {
          builder.append(" JOIN ")
        }

        builder.append(table.toRawQuery)

        on match {
          case Some(cond) =>
            builder.append(" ON ")
            cond.toRawQuery(builder, values)
          case _ =>
        }

        next match { case Some(nextFromTable) => nextFromTable.toRawQuery(builder, values) case _ => }
      }
    }

    def from[A](table: Table[A]) = {
      firstFromTable = new FromTable(table)
      lastJoinTable = firstFromTable
      this
    }

    def join[A](table: Table[A], on: Condition) = {
      lastJoinTable = lastJoinTable.join(new FromTable(table, Some(on)))

      this
    }

    override def build(rawQuery: StringBuilder, values: ListBuffer[Any]) = {
      rawQuery.append("DELETE ")

      rawQuery.append(" FROM ")
      firstFromTable.toRawQuery(rawQuery, values)

      var first = true

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
