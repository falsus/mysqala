package com.github.falsus.mysqala.nameresolver

import java.lang.reflect.Field

trait CamelToUnderScoreNameResolver extends NameResolver {
  override def resolveTable[A](tableClass: Class[A]): Option[String] = {
    Some(camelToUnderScore(tableClass.getSimpleName))
  }

  override def resolveField[A](tableClass: Class[A], field: Field): Option[String] = {
    Some(camelToUnderScore(field.getName))
  }

  def camelToUnderScore(name: String) = {
    val buf = new StringBuilder()
    var prevUpperCase = true

    for (c <- name) {
      if (c.isUpper) {
        if (!prevUpperCase) {
          buf.append("_")
        }

        buf.append(c.toLowerCase)
        prevUpperCase = true
      } else {
        prevUpperCase = false
        buf.append(c)
      }
    }

    buf.toString
  }
}
