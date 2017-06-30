package com.github.falsus.mysqala.condition

import scala.collection.mutable.ListBuffer

abstract class Condition {
  var isAnd: Boolean = false
  var nextCond: Condition = _

  def toRawQueryChild(values: ListBuffer[Any]): String

  def toRawQuery(values: ListBuffer[Any]): String = {
    if (nextCond == null) {
      return toRawQueryChild(values)
    }

    toRawQueryChild(values) + (if (isAnd) " AND " else " OR ") + nextCond.toRawQuery(values)
  }

  def hasNext: Boolean = nextCond != null

  def length: Int = count(1)

  def count(total: Int): Int = if (nextCond == null) total else nextCond.count(total + 1)

  def and(cond: Condition): Condition = {
    nextCond = cond
    isAnd = true
    this
  }

  def or(cond: Condition): Condition = {
    nextCond = cond
    isAnd = false
    this
  }

  def AND(cond: Condition): Condition = and(cond)

  def OR(cond: Condition): Condition = or(cond)
}