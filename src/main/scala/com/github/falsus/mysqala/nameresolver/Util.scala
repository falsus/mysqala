package com.github.falsus.mysqala.nameresolver

object Util {
  def toPluralForm(name: String) = {
    if (name.endsWith("s")) {
      name + "es"
    } else if (name.endsWith("y")) {
      name.substring(0, name.length - 1) + "ies"
    } else {
      name + "s"
    }
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