package com.github.falsus.mysqala

import table.{ Table, TableImpl }
import connection.SingleConnectionManager
import selectable.{ Column, OrderedColumn }
import util.Using
import query.test.model.{ User, Message }
import query.test.table.{ UserTable, MessageTable }

package query {
  import org.specs.SpecificationWithJUnit
  import org.specs.runner.JUnitSuiteRunner
  import org.junit.runner.RunWith

  import scala.collection.mutable.ListBuffer

  @RunWith(classOf[JUnitSuiteRunner])
  class DeleteQueryTest extends SpecificationWithJUnit with Using {
    table.Table.simpleTableNames.clear()
    org.h2.Driver.load()
    
    val connection = java.sql.DriverManager.getConnection("jdbc:h2:mem:DeleteQueryTest;MODE=MySQL")

    using(connection.prepareStatement("DROP TABLE IF EXISTS users;CREATE TABLE users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
      stmt.executeUpdate()
    }

    using(connection.prepareStatement("DROP TABLE IF EXISTS messages;CREATE TABLE messages(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, parent_message_id INT, message VARCHAR(255))")) { stmt =>
      stmt.executeUpdate()
    }

    val users = new UserTable(new SingleConnectionManager(connection))
    val messages = new MessageTable(new SingleConnectionManager(connection))

    "Callable SQL like command" in {
      val q = users.DELETE FROM users
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "DELETE FROM users u" must be equalTo rawQuery.toString
      values.length must be equalTo 0
    }

    "Callable SQL like command with WHERE" in {
      val id = users.getIntColumn("id")
      val q = users.DELETE FROM users WHERE id == 10
      val values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "DELETE FROM users u WHERE u.id = ?" must be equalTo rawQuery.toString
      values.length must be equalTo 1
      values(0) must be equalTo 10
    }

    "Callable SQL like command with JOIN" in {
      val messageTableForInnerJoin = messages.cloneForInnerJoin
      val id = users.getIntColumn("id")
      val userId = messages.getIntColumn("userId")
      val messageId = messages.getIntColumn("id")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = users.DELETE FROM users JOIN messages ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "DELETE FROM users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id" must be equalTo rawQuery.toString
    }

    "Callable SQL like command with WHERE, AND, OR, JOIN" in {
      val messageTableForInnerJoin = messages.cloneForInnerJoin
      val id = users.getIntColumn("id")
      val userId = messages.getIntColumn("userId")
      val messageId = messages.getIntColumn("id")
      val message = messages.getStringColumn("message")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = users.DELETE FROM users JOIN messages ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId WHERE id == 8 OR (messageId == 10 AND message == "hello")
      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "DELETE FROM users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id WHERE u.id = ? OR (m.id = ? AND m.message = ?)" must be equalTo rawQuery.toString
      values.length must be equalTo 3
      values(0) must be equalTo 8
      values(1) must be equalTo 10
      values(2) must be equalTo "hello"
    }
  }
}