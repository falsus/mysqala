package com.github.falsus.mysqala

import connection.ConnectionManager
import util.Using
import table.Table
import condition.{ SameValueCondition }

package query {
  import scala.collection.mutable.ListBuffer

  class UpdateQuery(val connManager: ConnectionManager, tables_ : Table[_]*) extends WhereQuery[UpdateQuery] with Using {
    private def conn = connManager.connection
    private val tables = tables_.init
    val subInstance = this
    var setters: Seq[SameValueCondition[_, _]] = null

    from(tables_.last)

    def set(sets: SameValueCondition[_, _]*) = {
      setters = sets
      this
    }

    def SET(sets: SameValueCondition[_, _]*) = set(sets: _*)

    override def build(values: ListBuffer[Any]): String = {
      "UPDATE " + tables.mkString(", ") + (if (tables.isEmpty) "" else ", ") + firstFromTable.toRawQuery(values) + " SET " +
        setters.map { setter => setter.toRawQuery(values) }.mkString(", ") +
        buildWhere(values) +
        buildOrder(values) +
        buildLimit(values)
    }

    override def executeUpdate(): Int = {
      var values = ListBuffer[Any]()

      using(conn.prepareStatement(build(values))) { stmt =>
        setValues(stmt, values, 1)
        stmt.executeUpdate()
      }
    }
  }
}
