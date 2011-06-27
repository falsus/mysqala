package com.github.falsus.mysqala

import org.specs.SpecificationWithJUnit
import org.specs.runner.JUnitSuiteRunner
import org.junit.runner.RunWith

import scala.collection.mutable.ListBuffer

import table.{ Table, TableImpl }
import connection.SingleConnectionManager
import selectable.{ Column, OrderedColumn }
import util.Using
import query.test.model.{ User, Message }
import query.test.table.{ UserTable, MessageTable }

package query {
  class InsertQueryTest extends SpecificationWithJUnit with Using {
    table.Table.simpleTableNames.clear()
    
    val users = new UserTable(new SingleConnectionManager(connection))
    val messages = new MessageTable(new SingleConnectionManager(connection))

    lazy val connection = {
      org.h2.Driver.load()

      java.sql.DriverManager.getConnection("jdbc:h2:mem:test;MODE=MySQL")
    }

    using(connection.prepareStatement("DROP TABLE IF EXISTS users;CREATE TABLE users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
      stmt.executeUpdate()
    }

    using(connection.prepareStatement("DROP TABLE IF EXISTS messages;CREATE TABLE messages(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, parent_message_id INT, message VARCHAR(255))")) { stmt =>
      stmt.executeUpdate()
    }

    "build" should {
      "Callable SQL like command" in {
        val id = users.getIntColumn("id")
        val q = users.INSERT INTO (id) VALUES (10)
        val values = ListBuffer[Any]()
        val rawQuery = new StringBuilder()

        q.build(rawQuery, values)

        "INSERT INTO users(id) VALUES(?)" must be equalTo rawQuery.toString
        values.length must be equalTo 1
        values(0) must be equalTo 10
      }

      "Callable SQL like command with SELECT" in {
        val id = users.getIntColumn("id")
        val q = users.INSERT INTO (id) SELECT (id) FROM users WHERE id == 20
        val values = ListBuffer[Any]()
        val rawQuery = new StringBuilder()
        val tableName = users.shortDatabaseTableName

        q.build(rawQuery, values)

        "INSERT INTO users(id) SELECT " + tableName + ".id FROM users " + tableName + " WHERE " + tableName + ".id = ?" must be equalTo rawQuery.toString
        values.length must be equalTo 1
        values(0) must be equalTo 20
      }
    }
  }
}