package com.github.falsus.mysqala.condition

import scala.collection.mutable.{ ListBuffer, LinkedHashMap }

abstract class Condition {
  var isAnd: Boolean = false
  var nextCond: Condition = null

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
  
  def length = {
    count(1)
  }
  
  def count(total: Int): Int = {
    if (nextCond == null) {
      total
    } else {
      nextCond.count(total + 1)
    }
  }

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

  def AND(cond: Condition) = and(cond)
  def OR(cond: Condition) = or(cond)
}