package com.github.falsus.mysqala

import com.github.falsus.mysqala.condition.SameValueCondition
import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.table.Table
import com.github.falsus.mysqala.util.Using

package query {

  import scala.collection.mutable.ListBuffer

  class UpdateQuery(val connManager: ConnectionManager, tables_ : Table[_]*) extends WhereQuery[UpdateQuery] with Using {
    private def conn = connManager.connection

    private val tables = tables_.init
    val subInstance: UpdateQuery = this
    var setters: Seq[SameValueCondition[_, _]] = _

    from(tables_.last)

    def set(sets: SameValueCondition[_, _]*): UpdateQuery = {
      setters = sets
      this
    }

    def SET(sets: SameValueCondition[_, _]*): UpdateQuery = set(sets: _*)

    override def build(values: ListBuffer[Any]): String = {
      "UPDATE " + tables.mkString(", ") + (if (tables.isEmpty) "" else ", ") + firstFromTable.toRawQuery(values) + " SET " +
        setters.map { setter => setter.toRawQuery(values) }.mkString(", ") +
        buildWhere(values) +
        buildOrder(values) +
        buildLimit(values)
    }

    override def executeUpdate(): Int = {
      val values = ListBuffer[Any]()

      using(conn.prepareStatement(build(values))) { stmt =>
        setValues(stmt, values, 1)
        stmt.executeUpdate()
      }
    }
  }

}
