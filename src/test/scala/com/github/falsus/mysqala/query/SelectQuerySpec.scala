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
  class User(val id: Int, val name: String)
  object UserTable extends TableImpl[User](new SingleConnectionManager(SelectQuerySpec.connection), classOf[User])

  class Message(val id: Int, val userId: Int, val parentMessageId: Int, val message: String)
  object MessageTable extends TableImpl[Message](new SingleConnectionManager(SelectQuerySpec.connection), classOf[Message])

  @RunWith(classOf[JUnitSuiteRunner])
  object SelectQuerySpec extends SpecificationWithJUnit with Using {
    lazy val connection = {
      org.h2.Driver.load()

      java.sql.DriverManager.getConnection("jdbc:h2:mem:test;MODE=MySQL")
    }

    using(SelectQuerySpec.connection.prepareStatement("DROP TABLE IF EXISTS users;CREATE TABLE users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
      stmt.executeUpdate()
    }

    using(SelectQuerySpec.connection.prepareStatement("DROP TABLE IF EXISTS messages;CREATE TABLE messages(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, parent_message_id INT, message VARCHAR(255))")) { stmt =>
      stmt.executeUpdate()
    }

    "Callable SQL like command" in {
      val q = new SelectQuery(None, SelectQuerySpec.connection, UserTable.*)

      q FROM UserTable

      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u" must be equalTo rawQuery.toString
      values.length must be equalTo 0
    }

    "Callable SQL like command with WHERE" in {
      val q = new SelectQuery(None, SelectQuerySpec.connection, UserTable.*)
      val id = UserTable.getIntColumn("id")

      q FROM UserTable WHERE id == 10

      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u WHERE (u.id = ?)" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 10
    }

    "Callable SQL like command with ORDER BY" in {
      val q = new SelectQuery(None, SelectQuerySpec.connection, UserTable.*)
      val id = UserTable.getIntColumn("id")

      q FROM UserTable ORDER_BY (id ASC)

      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u ORDER BY u.id" must be equalTo rawQuery.toString
      values.length must be equalTo 0
    }

    "Callable SQL like command with ORDER BY DESC" in {
      val q = new SelectQuery(None, SelectQuerySpec.connection, UserTable.*)
      val id = UserTable.getIntColumn("id")
      val name = UserTable.getStringColumn("name")

      q FROM UserTable ORDER_BY (id DESC, name ASC)

      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u ORDER BY u.id DESC, u.name" must be equalTo rawQuery.toString
      values.length must be equalTo 0
    }

    "Callable SQL like command with LIMIT" in {
      val q = new SelectQuery(None, SelectQuerySpec.connection, UserTable.*)
      val id = UserTable.getIntColumn("id")

      q FROM UserTable LIMIT 100

      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u LIMIT ?" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 100
    }

    "Callable SQL like command with LIMIT OFFSET" in {
      val q = new SelectQuery(None, SelectQuerySpec.connection, UserTable.*)
      val id = UserTable.getIntColumn("id")

      q FROM UserTable LIMIT 100 OFFSET 10

      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u LIMIT ? OFFSET ?" must be equalTo rawQuery.toString
      values.length must be equalTo 2
      values(0) must be equalTo 100
      values(1) must be equalTo 10
    }

    "Callable SQL like command with JOIN" in {
      val messageTableForInnerJoin = MessageTable.cloneForInnerJoin
      val q = new SelectQuery(None, SelectQuerySpec.connection, UserTable.*)
      val id = UserTable.getIntColumn("id")
      val userId = MessageTable.getIntColumn("userId")
      val messageId = MessageTable.getIntColumn("id")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")

      q FROM UserTable JOIN MessageTable ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId

      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u JOIN messages m ON u.id = m.user_id JOIN messages m1 ON m.id = m1.parent_message_id" must be equalTo rawQuery.toString
    }
  }
}