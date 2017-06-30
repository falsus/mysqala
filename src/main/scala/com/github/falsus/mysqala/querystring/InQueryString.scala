package com.github.falsus.mysqala

import com.github.falsus.mysqala.selectable.Column

package querystring {

  import scala.collection.mutable.ListBuffer

  class InQueryString(column: Column[_, _]) extends DynamicQueryString {
    def build(values: ListBuffer[_]): String = {
      val value = values.remove(0)

      column.toRawQuery +
        (value match {
          case l: Seq[_] => " IN(" + l.map { _ => "?" }.mkString(", ") + ")"
          case _ => throw new Exception("must be List")
        })
    }
  }

}