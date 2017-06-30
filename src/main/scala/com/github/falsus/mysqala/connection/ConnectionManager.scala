package com.github.falsus.mysqala.connection

import java.sql.Connection

trait ConnectionManager {
  def connection: Connection

  def connection(master: Boolean, databaseType: Int = 0): Connection = connection
}

class SingleConnectionManager(val conn: Connection) extends ConnectionManager {
  def connection: Connection = conn
}