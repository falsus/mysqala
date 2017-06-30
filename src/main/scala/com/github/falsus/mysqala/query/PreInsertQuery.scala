package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.selectable.Column
import com.github.falsus.mysqala.table.Table

package query {

  class PreInsertQuery[A](tbl: Table[A], val connManager: ConnectionManager) {
    def into[B](col1: Column[A, B]) = new InsertQuery1(connManager, tbl, col1)

    def into[B, C](col1: Column[A, B], col2: Column[A, C]) = new InsertQuery2(connManager, tbl, col1, col2)

    def into[B, C, D](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D]) = new InsertQuery3(connManager, tbl, col1, col2, col3)

    def into[B, C, D, E](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E]) = new InsertQuery4(connManager, tbl, col1, col2, col3, col4)

    def into[B, C, D, E, F](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F]) = new InsertQuery5(connManager, tbl, col1, col2, col3, col4, col5)

    def into[B, C, D, E, F, G](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G]) = new InsertQuery6(connManager, tbl, col1, col2, col3, col4, col5, col6)

    def into[B, C, D, E, F, G, H](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H]) = new InsertQuery7(connManager, tbl, col1, col2, col3, col4, col5, col6, col7)

    def into[B, C, D, E, F, G, H, I](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H], col8: Column[A, I]) = new InsertQuery8(connManager, tbl, col1, col2, col3, col4, col5, col6, col7, col8)

    def INTO[B](col1: Column[A, B]): InsertQuery1[A, B] = into(col1)

    def INTO[B, C](col1: Column[A, B], col2: Column[A, C]): InsertQuery2[A, B, C] = into(col1, col2)

    def INTO[B, C, D](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D]): InsertQuery3[A, B, C, D] = into(col1, col2, col3)

    def INTO[B, C, D, E](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E]): InsertQuery4[A, B, C, D, E] = into(col1, col2, col3, col4)

    def INTO[B, C, D, E, F](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F]): InsertQuery5[A, B, C, D, E, F] = into(col1, col2, col3, col4, col5)

    def INTO[B, C, D, E, F, G](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G]): InsertQuery6[A, B, C, D, E, F, G] = into(col1, col2, col3, col4, col5, col6)

    def INTO[B, C, D, E, F, G, H](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H]): InsertQuery7[A, B, C, D, E, F, G, H] = into(col1, col2, col3, col4, col5, col6, col7)

    def INTO[B, C, D, E, F, G, H, I](col1: Column[A, B], col2: Column[A, C], col3: Column[A, D], col4: Column[A, E], col5: Column[A, F], col6: Column[A, G], col7: Column[A, H], col8: Column[A, I]): InsertQuery8[A, B, C, D, E, F, G, H, I] = into(col1, col2, col3, col4, col5, col6, col7, col8)
  }

}
