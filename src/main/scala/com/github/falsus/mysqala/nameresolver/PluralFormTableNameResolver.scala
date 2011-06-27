package com.github.falsus.mysqala.nameresolver

trait PluralFormTableNameResolver extends NameResolver {
  override def resolveTable[A](tableClass: Class[A]): Option[String] = {
    Some(Util.toPluralForm(tableClass.getSimpleName))
  }
}
