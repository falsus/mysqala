package com.github.falsus.mysqala

import com.github.falsus.mysqala.condition.Condition
import com.github.falsus.mysqala.selectable.OrderedColumn
import com.github.falsus.mysqala.table.Table

package query {

  import scala.collection.mutable.ListBuffer

  abstract class WhereQuery[A] extends Query {
    protected def subInstance: A

    protected var firstFromTable: FromTable[_] = _
    private var firstWhereCondition: Condition = _
    private var orderedColumns: Seq[OrderedColumn] = _
    private var whereConditions: ListBuffer[(Condition, Boolean)] = ListBuffer[(Condition, Boolean)]()
    private var limitOption: Option[Int] = None
    private var offsetOption: Option[Int] = None
    private var lastJoinTable: FromTable[_] = _

    class FromTable[A](val table: Table[A], var on: Option[Condition] = None) {
      var next: Option[FromTable[_]] = None

      def nextTable = next

      def join[B](joinTable: FromTable[B]) = {
        next = Some(joinTable)
        joinTable
      }

      def toRawQuery(values: ListBuffer[Any]): String = {
        (if (on.nonEmpty) " JOIN " else "") + table.toRawQuery +
          (on match {
            case Some(cond) => " ON " + cond.toRawQuery(values)
            case _ => ""
          }) +
          (next match {
            case Some(nextFromTable) => nextFromTable.toRawQuery(values)
            case _ => ""
          })
      }
    }

    def FROM[A](table: Table[A]) = from(table)

    def JOIN[A](table: Table[A]) = join(table)

    def ON(on_ : Condition) = on(on_)

    def WHERE(cond: Condition) = where(cond)

    def OR(cond: Condition) = or(cond)

    def AND(cond: Condition) = and(cond)

    def from[A](table: Table[A]) = {
      firstFromTable = new FromTable(table)
      lastJoinTable = firstFromTable
      subInstance
    }

    def join[A](table: Table[A]) = {
      lastJoinTable = lastJoinTable.join(new FromTable(table))
      subInstance
    }

    def on(on_ : Condition) = {
      lastJoinTable.on = Some(on_)
      subInstance
    }

    def where(cond: Condition) = {
      firstWhereCondition = cond
      whereConditions += ((cond, false))
      subInstance
    }

    def or(cond: Condition) = {
      whereConditions += ((cond, false))
      subInstance
    }

    def and(cond: Condition) = {
      whereConditions += ((cond, true))
      subInstance
    }

    def orderBy(columns: OrderedColumn*) = {
      orderedColumns = columns
      subInstance
    }

    def order_by(columns: OrderedColumn*) = orderBy(columns: _*)

    def limit(lim: Int) = {
      limitOption = Some(lim)
      subInstance
    }

    def offset(off: Int) = {
      offsetOption = Some(off)
      subInstance
    }

    def ORDER_BY(columns: OrderedColumn*) = order_by(columns: _*)

    def LIMIT(lim: Int) = limit(lim)

    def OFFSET(off: Int) = offset(off)

    def buildWhere(values: ListBuffer[Any]): String = {
      if (firstWhereCondition == null || whereConditions.isEmpty) {
        return ""
      }

      whereConditions.foldLeft("") { (prev, cur) =>
        prev +
          (cur match {
            case (condition, and) =>
              (if (prev.isEmpty) " WHERE " else if (and) {
                " AND "
              } else {
                " OR "
              }) +
                (if (condition.hasNext) {
                  "("
                } else {
                  ""
                }) +
                condition.toRawQuery(values) +
                (if (condition.hasNext) {
                  ")"
                } else {
                  ""
                })
          })
      }
    }

    def buildOrder(values: ListBuffer[Any]): String = {
      if (orderedColumns == null) {
        return ""
      }

      " ORDER BY " +
        orderedColumns.map { col => col.column.toRawQuery + (if (!col.asc) " DESC" else "") }.mkString(", ")
    }

    def buildLimit(values: ListBuffer[Any]): String = {
      if (limitOption.isEmpty) {
        return ""
      }

      values += limitOption.get
      " LIMIT ?"
    }

    def buildOffset(values: ListBuffer[Any]): String = {
      if (offsetOption.isEmpty) {
        return ""
      }

      values += offsetOption.get
      " OFFSET ?"
    }
  }

}
