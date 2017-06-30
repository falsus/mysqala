package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.SingleConnectionManager
import com.github.falsus.mysqala.query.test.table.{MessageTable, UserTable}
import com.github.falsus.mysqala.util.Using

import scala.collection.mutable.ListBuffer

package query {

  import java.util.Properties

  import org.h2.jdbc.JdbcConnection
  import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

  class SelectQueryTest extends FlatSpec with Matchers with Using with BeforeAndAfterAll {
    org.h2.Driver.load()
    val connection = new JdbcConnection("jdbc:h2:mem:SelectQueryTest;MODE=MySQL", new Properties())
    lazy val users = new UserTable(new SingleConnectionManager(connection))
    lazy val messages = new MessageTable(new SingleConnectionManager(connection))

    override def beforeAll() = {
      table.Table.simpleTableNames.clear()

      using(connection.prepareStatement("DROP TABLE IF EXISTS users;CREATE TABLE users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
        stmt.executeUpdate()
      }

      using(connection.prepareStatement("DROP TABLE IF EXISTS messages;CREATE TABLE messages(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, parent_message_id INT, message VARCHAR(255))")) { stmt =>
        stmt.executeUpdate()
      }
    }

    "SelectQuery" should "Callable SQL like command" in {
      val q = users.SELECT(users.*) FROM users
      val values = ListBuffer[Any]()

      "SELECT u.* FROM users u" should be(q.build(values))
      values.length should be(0)
    }

    "SelectQuery" should "Callable SQL like command with WHERE" in {
      val id = users.getIntColumn("id")
      val q = users.SELECT(users.*) FROM users WHERE id == Some(10)
      val values = ListBuffer[Any]()

      "SELECT u.* FROM users u WHERE u.id = ?" should be(q.build(values))
      values.length should be(1)
      values(0) should be(10)
    }

    "SelectQuery" should "Callable SQL like command with ORDER BY" in {
      val id = users.getIntColumn("id")
      val q = users.SELECT(users.*) FROM users ORDER_BY (id ASC)
      val values = ListBuffer[Any]()

      "SELECT u.* FROM users u ORDER BY u.id" should be(q.build(values))
      values.length should be(0)
    }

    "SelectQuery" should "Callable SQL like command with ORDER BY DESC" in {
      val id = users.getIntColumn("id")
      val name = users.getStringColumn("name")
      val q = users.SELECT(users.*) FROM users ORDER_BY(id DESC, name ASC)
      val values = ListBuffer[Any]()

      "SELECT u.* FROM users u ORDER BY u.id DESC, u.name" should be(q.build(values))
      values.length should be(0)
    }

    "SelectQuery" should "Callable SQL like command with LIMIT" in {
      val id = users.getIntColumn("id")
      val q = users.SELECT(users.*) FROM users LIMIT 100
      var values = ListBuffer[Any]()

      "SELECT u.* FROM users u LIMIT ?" should be(q.build(values))
      values.length should be(1)
      values(0) should be(100)
    }

    "SelectQuery" should "Callable SQL like command with LIMIT OFFSET" in {
      val id = users.getIntColumn("id")
      val q = users.SELECT(users.*) FROM users LIMIT 100 OFFSET 10
      var values = ListBuffer[Any]()

      "SELECT u.* FROM users u LIMIT ? OFFSET ?" should be(q.build(values))
      values.length should be(2)
      values(0) should be(100)
      values(1) should be(10)
    }

    "SelectQuery" should "Callable SQL like command with JOIN" in {
      val messageTableForInnerJoin = messages.cloneForInnerJoin
      val id = users.getIntColumn("id")
      val userId = messages.getIntColumn("userId")
      val messageId = messages.getIntColumn("id")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = users.SELECT(users.*, messages.*, messageTableForInnerJoin.*) FROM users JOIN messages ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId
      var values = ListBuffer[Any]()

      "SELECT u.*, m.*, " + tableName2 + ".* FROM users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id" should be(q.build(values))
    }

    "SelectQuery" should "Callable SQL like command with WHERE, AND, OR, ORDER BY, LIMIT, OFFSET, JOIN" in {
      val messageTableForInnerJoin = messages.cloneForInnerJoin
      val id = users.getIntColumn("id")
      val userId = messages.getIntColumn("userId")
      val messageId = messages.getIntColumn("id")
      val message = messages.getStringColumn("message")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = users.SELECT(users.*) FROM users JOIN messages ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId WHERE id == Some(8) OR (messageId == Some(10) AND message == Some("hello")) ORDER_BY (id ASC) LIMIT 11 OFFSET 12
      val values = ListBuffer[Any]()

      "SELECT u.* FROM users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id WHERE u.id = ? OR (m.id = ? AND m.message = ?) ORDER BY u.id LIMIT ? OFFSET ?" should be(q.build(values))
      values.length should be(5)
      values(0) should be(8)
      values(1) should be(10)
      values(2) should be("hello")
      values(3) should be(11)
      values(4) should be(12)
    }
  }

}