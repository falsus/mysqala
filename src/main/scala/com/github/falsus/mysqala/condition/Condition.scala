package com.github.falsus.mysqala.condition

import scala.collection.mutable.{ ListBuffer, LinkedHashMap }

abstract class Condition {
  var isAnd: Boolean = false
  var nextCond: Condition = null

  def toRawQueryChild(values: ListBuffer[Any]): String

  def toRawQuery(values: ListBuffer[Any]): String = {
    if (nextCond == null) {
      return toRawQueryChild(values)
    }

    toRawQueryChild(values) + (if (isAnd) " AND " else " OR ") + nextCond.toRawQuery(values)
  }

  def hasNext = nextCond != null
  def length = count(1)
  def count(total: Int): Int = if (nextCond == null) total else nextCond.count(total + 1)

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