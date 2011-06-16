package com.github.falsus.mysqala.nameresolver

trait PluralFormTableNameResolver extends NameResolver {
  override def resolveTable[A](tableClass: Class[A]): Option[String] = {
    Some(toPluralForm(tableClass.getSimpleName))
  }

  def toPluralForm(name: String) = {
    if (name.endsWith("s")) {
      name + "es"
    } else if (name.endsWith("y")) {
      name.substring(0, name.length - 1) + "ies"
    } else {
      name + "s"
    }
  }
}
