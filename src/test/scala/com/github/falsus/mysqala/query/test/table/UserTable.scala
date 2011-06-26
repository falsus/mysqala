package com.github.falsus.mysqala

import query.test.model.User
import table.TableImpl
import connection.ConnectionManager

package query.test.table {
  class UserTable(connManager: ConnectionManager) extends TableImpl[User](connManager, classOf[User])
}