package com.github.falsus.mysqala

import java.sql.Connection

import scala.collection.mutable.ListBuffer

import org.junit.runner.RunWith
import org.specs.runner.JUnitSuiteRunner
import org.specs.SpecificationWithJUnit

import com.github.falsus.mysqala.connection.ConnectionManager

import condition.PlaceHolder
import connection.SingleConnectionManager
import query.test.model.Message
import query.test.model.User
import query.test.table.MessageTable
import query.test.table.UserTable
import querystring.FixedQueryString
import querystring.InQueryString
import selectable.Column
import table.Table
import table.TableImpl
import util.Using

package query {
  @RunWith(classOf[JUnitSuiteRunner])
  class FreezedSelectQueryTest extends SpecificationWithJUnit with Using {
    table.Table.simpleTableNames.clear()
    org.h2.Driver.load()

    val connection = java.sql.DriverManager.getConnection("jdbc:h2:mem:FreezedSelectQueryTest;MODE=MySQL")

    using(connection.prepareStatement("DROP TABLE IF EXISTS users;CREATE TABLE users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
      stmt.executeUpdate()
    }

    using(connection.prepareStatement("DROP TABLE IF EXISTS messages;CREATE TABLE messages(id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, parent_message_id INT, message VARCHAR(255))")) { stmt =>
      stmt.executeUpdate()
    }

    val users = new UserTable(new SingleConnectionManager(connection))
    val messages = new MessageTable(new SingleConnectionManager(connection))

    {
      val name = users.getStringColumn("name")
      (users.INSERT INTO name VALUES ("hello1")).executeUpdate()
      (users.INSERT INTO name VALUES ("hello2")).executeUpdate()
    }

    "FreezedSelectQuery" should {
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
            models(0) match {
              case u: User => foundModels.append(u)
            })
        }

        foundModels.length must be equalTo 2
        foundModels(0).name must be equalTo "hello1"
        foundModels(1).name must be equalTo "hello2"
      }

      "Freezable command with place holder" in {
        val constructors = classOf[User].getConstructors()
        val freezedQ = new FreezedSelectQuery(
          List(users.?),
          List(new FixedQueryString("SELECT u.* FROM users u WHERE name = ?")),
          Map(users -> (constructors(0), users.columns)),
          new SingleConnectionManager(connection))

        val foundModels = new ListBuffer[User]()

        freezedQ.execute("hello2") {
          (models =>
            models(0) match {
              case u: User => foundModels.append(u)
            })
        }

        foundModels.length must be equalTo 1
        foundModels(0).name must be equalTo "hello2"
      }

      "Freezable command with dynamic place holder" in {
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
              models(0) match {
                case u: User => foundModels.append(u)
              })
          }

          foundModels.length must be equalTo 1
          foundModels(0).name must be equalTo "hello2"
        }

        {
          val foundModels = new ListBuffer[User]()

          freezedQ.execute(List("hello1", "hello2")) {
            (models =>
              models(0) match {
                case u: User => foundModels.append(u)
              })
          }

          foundModels.length must be equalTo 2
          foundModels(0).name must be equalTo "hello1"
          foundModels(1).name must be equalTo "hello2"
        }
      }
    }
  }
}