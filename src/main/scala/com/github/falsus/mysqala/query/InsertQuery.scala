package com.github.falsus.mysqala

import selectable.{ Selectable, Column }
import table.Table
import util.Using

package query {
  import java.sql.Connection
  import scala.collection.mutable.ListBuffer

  abstract class InsertQuery[A](val conn: Connection, val table: Table[A], val columns: Column[A, _]*) extends Query with Using {
    var valueses = ListBuffer[List[Any]]()

    def select(columns: Selectable*) = {
      new SelectQuery(Some(this), conn, columns: _*)
    }

    override def build(rawQuery: StringBuilder, values: ListBuffer[Any]) = {
      rawQuery.append("INSERT INTO ")
      rawQuery.append(table.toRawQuerySingle)
      rawQuery.append("(")

      var first = true

      for (column <- columns) {
        if (first) {
          first = false
        } else {
          rawQuery.append(",")
        }

        rawQuery.append(column.toRawQuerySingle)
      }

      rawQuery.append(") VALUES")

      first = true

      for (vs <- valueses) {
        if (first) {
          first = false
        } else {
          rawQuery.append(", ")
        }

        rawQuery.append("(")

        var first2 = true

        for (v <- vs) {
          if (first2) {
            first2 = false
          } else {
            rawQuery.append(", ")
          }

          rawQuery.append("?")
          values += v
        }

        rawQuery.append(")")
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

  class InsertQuery1[A, B](conn: Connection, table: Table[A], col1: Column[A, B]) extends InsertQuery[A](conn, table, col1) {
    def values(value1: B) = {
      valueses += List(value1)
      this
    }
  }

  class InsertQuery2[A, B, C](conn: Connection, table: Table[A], col1: Column[A, B], col2: Column[A, C]) extends InsertQuery[A](conn, table, col1, col2) {
    def values(value1: B, value2: C) = {
      valueses += List(value1, value2)
      this
    }
  }

  class InsertQuery3[A, B, C, D](conn: Connection, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D]) extends InsertQuery[A](conn, table, col1, col2, col3) {
    def values(value1: B, value2: C, value3: D) = {
      valueses += List(value1, value2, value3)
      this
    }
  }

  class InsertQuery4[A, B, C, D, E](conn: Connection, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E]) extends InsertQuery[A](conn, table, col1, col2, col3, col4) {
    def values(value1: B, value2: C, value3: D, value4: E) = {
      valueses += List(value1, value2, value3, value4)
      this
    }
  }

  class InsertQuery5[A, B, C, D, E, F](conn: Connection, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F]) extends InsertQuery[A](conn, table, col1, col2, col3, col4, col5) {
    def values(value1: B, value2: C, value3: D, value4: E, value5: F) = {
      valueses += List(value1, value2, value3, value4, value5)
      this
    }
  }

  class InsertQuery6[A, B, C, D, E, F, G](conn: Connection, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G]) extends InsertQuery[A](conn, table, col1, col2, col3, col4, col5, col6) {
    def values(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G) = {
      valueses += List(value1, value2, value3, value4, value5, value6)
      this
    }
  }

  class InsertQuery7[A, B, C, D, E, F, G, H](conn: Connection, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H]) extends InsertQuery[A](conn, table, col1, col2, col3, col4, col5, col6, col7) {
    def values(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G, value7: H) = {
      valueses += List(value1, value2, value3, value4, value5, value6, value7)
      this
    }
  }

  class InsertQuery8[A, B, C, D, E, F, G, H, I](conn: Connection, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H], col8: Column[A, I]) extends InsertQuery[A](conn, table, col1, col2, col3, col4, col5, col6, col7, col8) {
    def values(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G, value7: H, value8: I) = {
      valueses += List(value1, value2, value3, value4, value5, value6, value7, value8)
      this
    }
  }
}
