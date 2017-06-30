package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.query.test.model.User
import com.github.falsus.mysqala.table.TableImpl

package query.test.table {

  class UserTable(connManager: ConnectionManager) extends TableImpl[User](connManager, classOf[User])

}