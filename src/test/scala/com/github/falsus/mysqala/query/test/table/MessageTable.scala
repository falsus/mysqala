package com.github.falsus.mysqala

import query.test.model.Message
import table.TableImpl
import connection.ConnectionManager

package query.test.table {
  class MessageTable(connManager: ConnectionManager) extends TableImpl[Message](connManager, classOf[Message])
}