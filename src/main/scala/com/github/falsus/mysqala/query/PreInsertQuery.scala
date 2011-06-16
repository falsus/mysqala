package com.github.falsus.mysqala

import selectable.Column
import table.Table

package query {
  import java.sql.Connection

  class PreInsertQuery[A](tbl: Table[A], val conn: Connection) {
    def into[B](col1: Column[A, B]) = {
      new InsertQuery1(conn, tbl, col1)
    }

    def into[B, C](col1: Column[A, B], col2: Column[A, C]) = {
      new InsertQuery2(conn, tbl, col1, col2)
    }

    def into[B, C, D](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D]) = {
      new InsertQuery3(conn, tbl, col1, col2, col3)
    }

    def into[B, C, D, E](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E]) = {
      new InsertQuery4(conn, tbl, col1, col2, col3, col4)
    }

    def into[B, C, D, E, F](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F]) = {
      new InsertQuery5(conn, tbl, col1, col2, col3, col4, col5)
    }

    def into[B, C, D, E, F, G](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G]) = {
      new InsertQuery6(conn, tbl, col1, col2, col3, col4, col5, col6)
    }

    def into[B, C, D, E, F, G, H](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H]) = {
      new InsertQuery7(conn, tbl, col1, col2, col3, col4, col5, col6, col7)
    }

    def into[B, C, D, E, F, G, H, I](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H], col8: Column[A, I]) = {
      new InsertQuery8(conn, tbl, col1, col2, col3, col4, col5, col6, col7, col8)
    }

    def INTO[B](col1: Column[A, B]) = into(col1)
    def INTO[B, C](col1: Column[A, B], col2: Column[A, C]) = into(col1, col2)
    def INTO[B, C, D](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D]) = into(col1, col2, col3)
    def INTO[B, C, D, E](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E]) = into(col1, col2, col3, col4)
    def INTO[B, C, D, E, F](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F]) = into(col1, col2, col3, col4, col5)
    def INTO[B, C, D, E, F, G](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G]) = into(col1, col2, col3, col4, col5, col6)
    def INTO[B, C, D, E, F, G, H](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H]) = into(col1, col2, col3, col4, col5, col6, col7)
    def INTO[B, C, D, E, F, G, H, I](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H], col8: Column[A, I]) = into(col1, col2, col3, col4, col5, col6, col7, col8)
  }
}
