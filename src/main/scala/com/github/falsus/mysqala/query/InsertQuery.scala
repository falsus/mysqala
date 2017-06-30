package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.selectable.{Column, Selectable}
import com.github.falsus.mysqala.table.Table
import com.github.falsus.mysqala.util.Using

package query {

  import scala.collection.mutable.ListBuffer

  abstract class InsertQuery[A](val connManager: ConnectionManager, val table: Table[A], val columns: Column[A, _]*) extends Query with Using {
    private val valueses = ListBuffer[List[Any]]()

    private def conn = connManager.connection

    def addValues(values: List[Any]): ListBuffer[List[Any]] = valueses += values

    def select(columns: Selectable*) = new SelectQuery(Some(this), connManager, columns: _*)

    def SELECT(columns: Selectable*): SelectQuery = select(columns: _*)

    override def build(values: ListBuffer[Any]): String = {
      "INSERT INTO " + table.toRawQuerySingle + "(" +
        columns.map { col => col.toRawQuerySingle }.mkString(", ") + ")" +
        (if (valueses.isEmpty) {
          ""
        } else {
          " VALUES" +
            valueses.map { vs =>
              "(" + vs.map {
                v =>
                  values += v
                  "?"
              }.mkString(", ") + ")"
            }.mkString(", ")
        })
    }

    override def executeUpdate(): Int = {
      val values = ListBuffer[Any]()

      using(conn.prepareStatement(build(values))) { stmt =>
        setValues(stmt, values, 1)
        stmt.executeUpdate()
      }
    }
  }

  class InsertQuery1[A, B](connManager: ConnectionManager, table: Table[A], col1: Column[A, B]) extends InsertQuery[A](connManager, table, col1) {
    def values(value1: B): InsertQuery1[A, B] = {
      addValues(List(value1))
      this
    }

    def VALUES(value1: B): InsertQuery1[A, B] = values(value1)
  }

  class InsertQuery2[A, B, C](connManager: ConnectionManager, table: Table[A], col1: Column[A, B], col2: Column[A, C]) extends InsertQuery[A](connManager, table, col1, col2) {
    def values(value1: B, value2: C): InsertQuery2[A, B, C] = {
      addValues(List(value1, value2))
      this
    }

    def VALUES(value1: B, value2: C): InsertQuery2[A, B, C] = values(value1, value2)
  }

  class InsertQuery3[A, B, C, D](connManager: ConnectionManager, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D]) extends InsertQuery[A](connManager, table, col1, col2, col3) {
    def values(value1: B, value2: C, value3: D): InsertQuery3[A, B, C, D] = {
      addValues(List(value1, value2, value3))
      this
    }

    def VALUES(value1: B, value2: C, value3: D): InsertQuery3[A, B, C, D] = values(value1, value2, value3)
  }

  class InsertQuery4[A, B, C, D, E](connManager: ConnectionManager, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E]) extends InsertQuery[A](connManager, table, col1, col2, col3, col4) {
    def values(value1: B, value2: C, value3: D, value4: E): InsertQuery4[A, B, C, D, E] = {
      addValues(List(value1, value2, value3, value4))
      this
    }

    def VALUES(value1: B, value2: C, value3: D, value4: E): InsertQuery4[A, B, C, D, E] = values(value1, value2, value3, value4)
  }

  class InsertQuery5[A, B, C, D, E, F](connManager: ConnectionManager, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F]) extends InsertQuery[A](connManager, table, col1, col2, col3, col4, col5) {
    def values(value1: B, value2: C, value3: D, value4: E, value5: F): InsertQuery5[A, B, C, D, E, F] = {
      addValues(List(value1, value2, value3, value4, value5))
      this
    }

    def VALUES(value1: B, value2: C, value3: D, value4: E, value5: F): InsertQuery5[A, B, C, D, E, F] = values(value1, value2, value3, value4, value5)
  }

  class InsertQuery6[A, B, C, D, E, F, G](connManager: ConnectionManager, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G]) extends InsertQuery[A](connManager, table, col1, col2, col3, col4, col5, col6) {
    def values(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G): InsertQuery6[A, B, C, D, E, F, G] = {
      addValues(List(value1, value2, value3, value4, value5, value6))
      this
    }

    def VALUES(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G): InsertQuery6[A, B, C, D, E, F, G] = values(value1, value2, value3, value4, value5, value6)
  }

  class InsertQuery7[A, B, C, D, E, F, G, H](connManager: ConnectionManager, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H]) extends InsertQuery[A](connManager, table, col1, col2, col3, col4, col5, col6, col7) {
    def values(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G, value7: H): InsertQuery7[A, B, C, D, E, F, G, H] = {
      addValues(List(value1, value2, value3, value4, value5, value6, value7))
      this
    }

    def VALUES(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G, value7: H): InsertQuery7[A, B, C, D, E, F, G, H] = values(value1, value2, value3, value4, value5, value6, value7)
  }

  class InsertQuery8[A, B, C, D, E, F, G, H, I](connManager: ConnectionManager, table: Table[A], col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H], col8: Column[A, I]) extends InsertQuery[A](connManager, table, col1, col2, col3, col4, col5, col6, col7, col8) {
    def values(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G, value7: H, value8: I): InsertQuery8[A, B, C, D, E, F, G, H, I] = {
      addValues(List(value1, value2, value3, value4, value5, value6, value7, value8))
      this
    }

    def VALUES(value1: B, value2: C, value3: D, value4: E, value5: F, value6: G, value7: H, value8: I): InsertQuery8[A, B, C, D, E, F, G, H, I] = values(value1, value2, value3, value4, value5, value6, value7, value8)
  }

}
