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
  object UpdateQuerySpec extends SpecificationWithJUnit with Using {
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
      val q = UserTable.UPDATE(UserTable) SET id == 10
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "UPDATE users u SET u.id = ?" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 10
    }

    "Callable SQL like command with WHERE" in {
      val id = UserTable.getIntColumn("id")
      val q = UserTable.UPDATE(UserTable) SET id == 20 WHERE id == 10
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "UPDATE users u SET u.id = ? WHERE u.id = ?" must be equalTo rawQuery.toString
      values.length must be equalTo 2
      values(0) must be equalTo 20
      values(1) must be equalTo 10
    }

    "Callable SQL like command with JOIN" in {
      val messageTableForInnerJoin = MessageTable.cloneForInnerJoin
      val id = UserTable.getIntColumn("id")
      val userId = MessageTable.getIntColumn("userId")
      val messageId = MessageTable.getIntColumn("id")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = UserTable.UPDATE(UserTable) JOIN MessageTable ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId SET id == 10
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "UPDATE users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id SET u.id = ?" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 10
    }

    "Callable SQL like command with WHERE, AND, OR, JOIN" in {
      val messageTableForInnerJoin = MessageTable.cloneForInnerJoin
      val id = UserTable.getIntColumn("id")
      val userId = MessageTable.getIntColumn("userId")
      val messageId = MessageTable.getIntColumn("id")
      val message = MessageTable.getStringColumn("message")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = UserTable.UPDATE(UserTable) JOIN MessageTable ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId SET (id == 10) WHERE id == 8 OR (messageId == 10 AND message == "hello")
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "UPDATE users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id SET u.id = ? WHERE u.id = ? OR (m.id = ? AND m.message = ?)" must be equalTo rawQuery.toString
      values.length must be equalTo 4
      values(0) must be equalTo 10
      values(1) must be equalTo 8
      values(2) must be equalTo 10
      values(3) must be equalTo "hello"
    }
  }
}