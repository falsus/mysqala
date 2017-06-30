package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.SingleConnectionManager
import com.github.falsus.mysqala.query.test.model.User
import com.github.falsus.mysqala.query.test.table.{MessageTable, UserTable}
import com.github.falsus.mysqala.querystring.{FixedQueryString, InQueryString}
import com.github.falsus.mysqala.util.Using

import scala.collection.mutable.ListBuffer

package query {

  import java.util.Properties

  import org.h2.jdbc.JdbcConnection
  import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

  class FreezedSelectQueryTest extends FlatSpec with Matchers with Using with BeforeAndAfterAll {
    org.h2.Driver.load()
    val connection = new JdbcConnection("jdbc:h2:mem:FreezedSelectQueryTest;MODE=MySQL", new Properties())
    lazy val users = new UserTable(new SingleConnectionManager(connection))
    lazy val messages = new MessageTable(new SingleConnectionManager(connection))
    lazy val name = users.getStringColumn("name")

    override def beforeAll() = {
      table.Table.simpleTableNames.clear()

      using(connection.prepareStatement("DROP TABLE IF EXISTS users;CREATE TABLE users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
        stmt.executeUpdate()
      }

      using(connection.prepareStatement("DROP TABLE IF EXISTS messages;CREATE TABLE messages(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, parent_message_id INT, message VARCHAR(255))")) { stmt =>
        stmt.executeUpdate()
      }

      (users.INSERT INTO name VALUES ("hello1")).executeUpdate()
      (users.INSERT INTO name VALUES ("hello2")).executeUpdate()
    }

    "FreezedSelectQuery" should
      "Freezable command" in {
      val constructors = classOf[User].getConstructors()
      val freezedQ = new FreezedSelectQuery(
        List(),
        List(new FixedQueryString("SELECT u.* FROM users u ORDER BY u.id")),
        Map(users -> (constructors(0), users.columns)),
        new SingleConnectionManager(connection))

      val foundModels = new ListBuffer[User]()

      freezedQ.execute() {
        (models =>
          models.head match {
            case u: User => foundModels.append(u)
          })
      }

      foundModels.length should be(2)
      foundModels(0).name should be("hello1")
      foundModels(1).name should be("hello2")
    }

    "FreezedSelectQuery" should "Freezable command with place holder" in {
      val constructors = classOf[User].getConstructors()
      val freezedQ = new FreezedSelectQuery(
        List(users.?),
        List(new FixedQueryString("SELECT u.* FROM users u WHERE name = ?")),
        Map(users -> (constructors(0), users.columns)),
        new SingleConnectionManager(connection))

      val foundModels = new ListBuffer[User]()

      freezedQ.execute("hello2") {
        (models =>
          models.head match {
            case u: User => foundModels.append(u)
          })
      }

      foundModels.length should be(1)
      foundModels(0).name should be("hello2")
    }

    "FreezedSelectQuery" should "Freezable command with dynamic place holder" in {
      val name = users.getStringColumn("name")
      val constructors = classOf[User].getConstructors()
      val freezedQ = new FreezedSelectQuery(
        List(users.?),
        List(new FixedQueryString("SELECT u.* FROM users u WHERE "),
          new InQueryString(name)),
        Map(users -> (constructors(0), users.columns)),
        new SingleConnectionManager(connection))

      {
        val foundModels = new ListBuffer[User]()

        freezedQ.execute(List("hello2")) {
          (models =>
            models.head match {
              case u: User => foundModels.append(u)
            })
        }

        foundModels.length should be(1)
        foundModels(0).name should be("hello2")
      }

      {
        val foundModels = new ListBuffer[User]()

        freezedQ.execute(List("hello1", "hello2")) {
          (models =>
            models.head match {
              case u: User => foundModels.append(u)
            })
        }

        foundModels.length should be(2)
        foundModels(0).name should be("hello1")
        foundModels(1).name should be("hello2")
      }
    }
  }

}