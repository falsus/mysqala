package com.github.falsus.mysqala.connection

import java.sql.Connection

trait ConnectionManager {
  def connection: Connection;
}