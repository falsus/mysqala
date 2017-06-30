package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.SingleConnectionManager
import com.github.falsus.mysqala.query.test.table.{MessageTable, UserTable}
import com.github.falsus.mysqala.util.Using

package query {

  import java.util.Properties

  import org.h2.jdbc.JdbcConnection
  import org.scalatest._

  import scala.collection.mutable.ListBuffer

  class DeleteQueryTest extends FlatSpec with Matchers with Using with BeforeAndAfterAll {
    org.h2.Driver.load()
    val connection = new JdbcConnection("jdbc:h2:mem:DeleteQueryTest;MODE=MySQL", new Properties())
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

    "DeleteQuery" should "Callable SQL like command" in {
      val q = users.DELETE FROM users
      val values = ListBuffer[Any]()

      "DELETE FROM users u" should be(q.build(values))
      values.length should be(0)
    }

    "DeleteQuery" should "Callable SQL like command with WHERE" in {
      val id = users.getIntColumn("id")
      val q = users.DELETE FROM users WHERE id == Some(10)
      val values = ListBuffer[Any]()

      "DELETE FROM users u WHERE u.id = ?" should be(q.build(values))
      values.length should be(1)
      values(0) should be(10)
    }

    "DeleteQuery" should "Callable SQL like command with JOIN" in {
      val messageTableForInnerJoin = messages.cloneForInnerJoin
      val id = users.getIntColumn("id")
      val userId = messages.getIntColumn("userId")
      val messageId = messages.getIntColumn("id")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = users.DELETE FROM users JOIN messages ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId
      var values = ListBuffer[Any]()

      "DELETE FROM users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id" should be(q.build(values))
    }

    "DeleteQuery" should "Callable SQL like command with WHERE, AND, OR, JOIN" in {
      val messageTableForInnerJoin = messages.cloneForInnerJoin
      val id = users.getIntColumn("id")
      val userId = messages.getIntColumn("userId")
      val messageId = messages.getIntColumn("id")
      val message = messages.getStringColumn("message")
      val parentMessageId = messageTableForInnerJoin.getIntColumn("parentMessageId")
      val tableName2 = messageTableForInnerJoin.shortDatabaseTableName
      val q = users.DELETE FROM users JOIN messages ON id == userId JOIN messageTableForInnerJoin ON messageId == parentMessageId WHERE id == Some(8) OR (messageId == Some(10) AND message == Some("hello"))
      var values = ListBuffer[Any]()

      "DELETE FROM users u JOIN messages m ON u.id = m.user_id JOIN messages " + tableName2 + " ON m.id = " + tableName2 + ".parent_message_id WHERE u.id = ? OR (m.id = ? AND m.message = ?)" should be(q.build(values))
      values.length should be(3)
      values(0) should be(8)
      values(1) should be(10)
      values(2) should be("hello")
    }
  }

}