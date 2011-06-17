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
  object SelectQuerySpec extends SpecificationWithJUnit with Using {
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
      val q = UserTable.SELECT(UserTable.*) FROM UserTable
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u" must be equalTo rawQuery.toString
      values.length must be equalTo 0
    }

    "Callable SQL like command with WHERE" in {
      val id = UserTable.getIntColumn("id")
      val q = UserTable.SELECT(UserTable.*) FROM UserTable WHERE id == 10
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u WHERE u.id = ?" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 10
    }

    "Callable SQL like command with ORDER BY" in {
      val id = UserTable.getIntColumn("id")
      val q = UserTable.SELECT(UserTable.*) FROM UserTable ORDER_BY (id ASC)
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u ORDER BY u.id" must be equalTo rawQuery.toString
      values.length must be equalTo 0
    }

    "Callable SQL like command with ORDER BY DESC" in {
      val id = UserTable.getIntColumn("id")
      val name = UserTable.getStringColumn("name")
      val q = UserTable.SELECT(UserTable.*) FROM UserTable ORDER_BY (id DESC, name ASC)
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u ORDER BY u.id DESC, u.name" must be equalTo rawQuery.toString
      values.length must be equalTo 0
    }

    "Callable SQL like command with LIMIT" in {
      val id = UserTable.getIntColumn("id")
      val q = UserTable.SELECT(UserTable.*) FROM UserTable LIMIT 100
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u LIMIT ?" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 100
    }

    "Callable SQL like command with LIMIT OFFSET" in {
      val id = UserTable.getIntColumn("id")
      val q = UserTable.SELECT(UserTable.*) FROM UserTable LIMIT 100 OFFSET 10
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
      val id = UserTable.getIntColumn("id")
      val userId = MessageTable.getIntColumn("userId")
      val messageId = MessageTable.getIntColumn("id")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = UserTable.SELECT(UserTable.*, MessageTable.*, messageTableForInnerJoin.*) FROM UserTable JOIN MessageTable ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.*, m.*, " + tableName2 + ".* FROM users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id" must be equalTo rawQuery.toString
    }

    "Callable SQL like command with WHERE, AND, OR, ORDER BY, LIMIT, OFFSET, JOIN" in {
      val messageTableForInnerJoin = MessageTable.cloneForInnerJoin
      val id = UserTable.getIntColumn("id")
      val userId = MessageTable.getIntColumn("userId")
      val messageId = MessageTable.getIntColumn("id")
      val message = MessageTable.getStringColumn("message")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = UserTable.SELECT(UserTable.*) FROM UserTable JOIN MessageTable ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId WHERE id == 8 OR (messageId == 10 AND message == "hello") ORDER_BY (id ASC) LIMIT 11 OFFSET 12
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id WHERE u.id = ? OR (m.id = ? AND m.message = ?) ORDER BY u.id LIMIT ? OFFSET ?" must be equalTo rawQuery.toString
      values.length must be equalTo 5
      values(0) must be equalTo 8
      values(1) must be equalTo 10
      values(2) must be equalTo "hello"
      values(3) must be equalTo 11
      values(4) must be equalTo 12
    }
  }
}