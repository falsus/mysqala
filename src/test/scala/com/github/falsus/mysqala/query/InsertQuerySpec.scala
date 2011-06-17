package com.github.falsus.mysqala

import org.specs.SpecificationWithJUnit
import org.specs.runner.JUnitSuiteRunner
import org.junit.runner.RunWith

import scala.collection.mutable.ListBuffer

import table.{ Table, TableImpl }
import connection.SingleConnectionManager
import selectable.{ Column, OrderedColumn }
import util.Using

package query {
  @RunWith(classOf[JUnitSuiteRunner])
  object InsertQuerySpec extends SpecificationWithJUnit with Using {
    class User(val id: Int, val name: String)
    object UserTable extends TableImpl[User](new SingleConnectionManager(connection), classOf[User])

    class Message(val id: Int, val userId: Int, val parentMessageId: Int, val message: String)
    object MessageTable extends TableImpl[Message](new SingleConnectionManager(connection), classOf[Message])

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

    "Callable SQL like command" in {
      val id = UserTable.getIntColumn("id")
      val q = UserTable.INSERT INTO (id) VALUES (10)
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "INSERT INTO users(id) VALUES(?)" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 10
    }

    "Callable SQL like command with SELECT" in {
      val id = UserTable.getIntColumn("id")
      val q = UserTable.INSERT INTO (id) SELECT (id) FROM UserTable WHERE id == 20
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "INSERT INTO users(id) SELECT u.id FROM users u WHERE u.id = ?" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 20
    }
  }
}