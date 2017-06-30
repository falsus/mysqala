package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.query.test.model.Message
import com.github.falsus.mysqala.table.TableImpl

package query.test.table {

  class MessageTable(connManager: ConnectionManager) extends TableImpl[Message](connManager, classOf[Message])

}