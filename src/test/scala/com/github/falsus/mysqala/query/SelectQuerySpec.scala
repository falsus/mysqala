package com.github.falsus.mysqala

import org.specs.Specification
import org.specs.runner.JUnit4
import junit.framework.TestResult

import scala.collection.mutable.ListBuffer

import table.{ Table, TableImpl }
import connection.SingleConnectionManager
import selectable.Column
import util.Using

package query {
  class User(val id: Int, val name: String) {
  }

  class UserTable extends TableImpl[User](new SingleConnectionManager(SelectQuerySpec.connection), classOf[User]) {
  }

  object SelectQuerySpec {
    lazy val connection = {
      org.h2.Driver.load()

      java.sql.DriverManager.getConnection("jdbc:h2:mem:test;MODE=MySQL")
    }
  }
  
  class SelectQuerySpec extends Specification with Using {
    "generate same query" in {
      createUsersTable()

      val users = new UserTable()
      val q = new SelectQuery(None, SelectQuerySpec.connection, users.*)

      q.from(users)

      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      q.build(rawQuery, values)

      "SELECT u.* FROM users u" must be equalTo (rawQuery.toString)
      values.length must be equalTo (0)
    }

    def createUsersTable() = {
      using(SelectQuerySpec.connection.prepareStatement("CREATE TABLE users(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))")) { stmt =>
        stmt.executeUpdate()
      }
    }
  }

  class SelectQueryTest extends JUnit4(new SelectQuerySpec())
}