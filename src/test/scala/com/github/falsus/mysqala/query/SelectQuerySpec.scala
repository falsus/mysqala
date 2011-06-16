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
  class User(val id: Int, val name: String) {
  }

  object UserTable extends TableImpl[User](new SingleConnectionManager(SelectQuerySpec.connection), classOf[User]) {
  }

  @RunWith(classOf[JUnitSuiteRunner])
  object SelectQuerySpec extends SpecificationWithJUnit with Using {
    lazy val connection = {
      org.h2.Driver.load()

      java.sql.DriverManager.getConnection("jdbc:h2:mem:test;MODE=MySQL")
    }

    using(SelectQuerySpec.connection.prepareStatement("CREATE TABLE IF NOT EXISTS users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
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
  }
}