package com.github.falsus.mysqala.nameresolver

object Util {
  def camelToUnderScore(name: String): String = {
    var prevUpperCase = true

    name.map { c =>
      if (c.isUpper) {
        (if (!prevUpperCase) {
          prevUpperCase = true
          "_"
        } else {
          ""
        }) + c.toLower
      } else {
        prevUpperCase = false
        c
      }
    }.mkString
  }

  def toPluralForm(name: String): String = {
    if (name.endsWith("s")) {
      name + "es"
    } else if (name.endsWith("y")) {
      name.substring(0, name.length - 1) + "ies"
    } else {
      name + "s"
    }
  }
}