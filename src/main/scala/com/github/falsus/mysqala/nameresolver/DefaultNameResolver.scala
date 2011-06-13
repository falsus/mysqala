package com.github.falsus.mysqala.nameresolver

class DefaultNameResolver extends CamelToUnderScoreNameResolver with PluralFormTableNameResolver {
  override def resolveTable[A](tableClass: Class[A]): Option[String] = {
    Some(toPluralForm(camelToUnderScore(tableClass.getSimpleName)))
  }
}
