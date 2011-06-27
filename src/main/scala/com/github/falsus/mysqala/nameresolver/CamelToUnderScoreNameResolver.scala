package com.github.falsus.mysqala.nameresolver

import java.lang.reflect.Field

trait CamelToUnderScoreNameResolver extends NameResolver {
  override def resolveTable[A](tableClass: Class[A]): Option[String] = {
    Some(Util.camelToUnderScore(tableClass.getSimpleName))
  }

  override def resolveField[A](tableClass: Class[A], field: Field): Option[String] = {
    Some(Util.camelToUnderScore(field.getName))
  }
}
