package com.github.falsus.mysqala

import condition.Condition
import selectable.{ Selectable, Column, OrderedColumn }

package query {
  import scala.collection.mutable.ListBuffer

  abstract class Query {
    var firstWhereCondition: Condition = null
    var orderedColumns: Seq[OrderedColumn] = null
    var whereConditions: ListBuffer[Tuple2[Condition, Boolean]] = ListBuffer[Tuple2[Condition, Boolean]]()
    var limitOption: Option[Int] = None
    var offsetOption: Option[Int] = None

    def execute(f: (List[Any]) => Unit): Unit = {
    }

    def executeUpdate(): Int = {
      0
    }

    def build(rawQuery: StringBuilder, values: ListBuffer[Any]): Unit

    def where(cond: Condition) = {
      firstWhereCondition = cond
      whereConditions += ((cond, false))
      this
    }

    def or(cond: Condition) = {
      whereConditions += ((cond, false))
      this
    }

    def and(cond: Condition) = {
      whereConditions += ((cond, true))
      this
    }

    def WHERE(cond: Condition) = where(cond)
    def OR(cond: Condition) = or(cond)
    def AND(cond: Condition) = and(cond)

    def orderBy(columns: OrderedColumn*) = {
      orderedColumns = columns
      this
    }

    def order_by(columns: OrderedColumn*) = {
      orderBy(columns: _*)
    }

    def limit(lim: Int) = {
      limitOption = Some(lim)
      this
    }

    def offset(off: Int) = {
      offsetOption = Some(off)
      this
    }

    def ORDER_BY(columns: OrderedColumn*) = order_by(columns: _*)
    def LIMIT(lim: Int) = limit(lim)
    def OFFSET(off: Int) = offset(off)
  }
}
