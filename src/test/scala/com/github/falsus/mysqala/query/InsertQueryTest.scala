package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.SingleConnectionManager
import com.github.falsus.mysqala.query.test.table.{MessageTable, UserTable}
import com.github.falsus.mysqala.util.Using

import scala.collection.mutable.ListBuffer

package query {

  import java.util.Properties

  import org.h2.jdbc.JdbcConnection
  import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

  class InsertQueryTest extends FlatSpec with Matchers with Using with BeforeAndAfterAll {
    org.h2.Driver.load()
    val connection = new JdbcConnection("jdbc:h2:mem:InsertQueryTest;MODE=MySQL", new Properties())
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

    "build" should
      "Callable SQL like command" in {
      val id = users.getIntColumn("id")
      val q = users.INSERT INTO (id) VALUES (10)
      val values = ListBuffer[Any]()

      "INSERT INTO users(id) VALUES(?)" should be(q.build(values))
      values.length should be(1)
      values(0) should be(10)
    }

    "build" should "Callable SQL like command with SELECT" in {
      val id = users.getIntColumn("id")
      val q = users.INSERT INTO (id) SELECT (id) FROM users WHERE id == Some(20)
      val values = ListBuffer[Any]()
      val tableName = users.shortDatabaseTableName

      "INSERT INTO users(id) SELECT " + tableName + ".id FROM users " + tableName + " WHERE " + tableName + ".id = ?" should be(q.build(values))
      values.length should be(1)
      values(0) should be(20)
    }
  }

}