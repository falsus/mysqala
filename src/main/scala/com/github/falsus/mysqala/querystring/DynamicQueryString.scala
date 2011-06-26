package com.github.falsus.mysqala.querystring

import scala.collection.mutable.ListBuffer

trait DynamicQueryString extends QueryString {
  def build(rawQuery: StringBuilder, values: ListBuffer[_]): Unit
}