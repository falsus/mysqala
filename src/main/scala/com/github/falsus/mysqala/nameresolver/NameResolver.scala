package com.github.falsus.mysqala.nameresolver

import java.lang.reflect.Field

trait NameResolver {
  def resolveTable[A](tableClass: Class[A]): Option[String]
  def resolveField[A](tableClass: Class[A], field: Field): Option[String]
}
