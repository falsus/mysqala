package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.SingleConnectionManager
import com.github.falsus.mysqala.query.test.table.{MessageTable, UserTable}
import com.github.falsus.mysqala.util.Using

import scala.collection.mutable.ListBuffer

package query {

  import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

  class UpdateQueryTest extends FlatSpec with Matchers with Using with BeforeAndAfterAll {
    val connection = java.sql.DriverManager.getConnection("jdbc:h2:mem:UpdateQueryTest;MODE=MySQL")
    lazy val users = new UserTable(new SingleConnectionManager(connection))
    lazy val messages = new MessageTable(new SingleConnectionManager(connection))

    override def beforeAll() = {
      table.Table.simpleTableNames.clear()
      org.h2.Driver.load()

      using(connection.prepareStatement("DROP TABLE IF EXISTS users;CREATE TABLE users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
        stmt.executeUpdate()
      }

      using(connection.prepareStatement("DROP TABLE IF EXISTS messages;CREATE TABLE messages(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, parent_message_id INT, message VARCHAR(255))")) { stmt =>
        stmt.executeUpdate()
      }
    }


    "UpdateQuery" should "Callable SQL like command" in {
      val id = users.getIntColumn("id")
      val q = users.UPDATE(users) SET id == 10
      val values = ListBuffer[Any]()

      "UPDATE users u SET u.id = ?" should be(q.build(values).toString)
      values.length should be(1)
      values(0) should be(10)
    }

    "UpdateQuery" should "Callable SQL like command with WHERE" in {
      val id = users.getIntColumn("id")
      val q = users.UPDATE(users) SET id == 20 WHERE id == 10
      val values = ListBuffer[Any]()

      "UPDATE users u SET u.id = ? WHERE u.id = ?" should be(q.build(values).toString)
      values.length should be(2)
      values(0) should be(20)
      values(1) should be(10)
    }

    "UpdateQuery" should "Callable SQL like command with JOIN" in {
      val messageTableForInnerJoin = messages.cloneForInnerJoin
      val id = users.getIntColumn("id")
      val userId = messages.getIntColumn("userId")
      val messageId = messages.getIntColumn("id")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = users.UPDATE(users) JOIN messages ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId SET id == 10
      var values = ListBuffer[Any]()

      "UPDATE users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id SET u.id = ?" should be(q.build(values))
      values.length should be(1)
      values(0) should be(10)
    }

    "UpdateQuery" should "Callable SQL like command with WHERE, AND, OR, JOIN" in {
      val messageTableForInnerJoin = messages.cloneForInnerJoin
      val id = users.getIntColumn("id")
      val userId = messages.getIntColumn("userId")
      val messageId = messages.getIntColumn("id")
      val message = messages.getStringColumn("message")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = users.UPDATE(users) JOIN messages ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId SET (id == 10) WHERE id == 8 OR (messageId == 10 AND message == "hello")
      var values = ListBuffer[Any]()

      "UPDATE users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id SET u.id = ? WHERE u.id = ? OR (m.id = ? AND m.message = ?)" should be(q.build(values))
      values.length should be(4)
      values(0) should be(10)
      values(1) should be(8)
      values(2) should be(10)
      values(3) should be("hello")
    }
  }

}