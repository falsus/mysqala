package com.github.falsus.mysqala.condition

import scala.collection.mutable.{ ListBuffer, LinkedHashMap }

abstract class Condition {
  def toRawQueryChild(builder: StringBuilder, values: ListBuffer[Any])
  def toRawQuery(builder: StringBuilder, values: ListBuffer[Any]) {
    toRawQueryChild(builder, values)

    if (nextCond != null) {
      if (isAnd) {
        builder.append(" AND ")
      } else {
        builder.append(" OR ")
      }

      nextCond.toRawQuery(builder, values)
    }
  }

  var isAnd: Boolean = false
  var nextCond: Condition = null

  def and(cond: Condition) = {
    nextCond = cond
    isAnd = true
    this
  }

  def or(cond: Condition) = {
    nextCond = cond
    isAnd = false
    this
  }
}
