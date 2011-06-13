package com.github.falsus.mysqala

import connection.ConnectionManager
import selectable.{ Selectable, Column, WildCardInTable, LastInsertId, IntColumn, StringColumn, LongColumn, DateColumn }
import util.Using
import nameresolver.{ NameResolver, DefaultNameResolver }
import query.{ SelectQuery, PreInsertQuery, DeleteQuery, UpdateQuery, SelectLastInsertIdQuery }
import condition.Condition

package table {
  import java.sql.{ ResultSet }
  import java.lang.reflect.Field
  import scala.collection.mutable.ListBuffer

  class Table[A](val connectionManager: ConnectionManager, val tableClass: Class[A], val nameResolver: NameResolver = new DefaultNameResolver()) {
    val (tableName, databaseName, columns) = MetaData.getMetaDatas(this, conn, tableClass, nameResolver)

    lazy val shortDatabaseTableName = Table.toSimpleTableNames(this)
    lazy val toRawQuery = tableName + " " + shortDatabaseTableName
    lazy val toRawQuerySingle = tableName

    private def conn = connectionManager.connection

    def select(columns: Selectable*) = {
      columns match {
        case Seq(column: LastInsertId) => new SelectLastInsertIdQuery(conn, columns: _*)
        case _ => new SelectQuery(None, conn, columns: _*)
      }
    }

    def last_insert_id() = {
      new LastInsertId()
    }

    lazy val delete = {
      new DeleteQuery(conn)
    }

    def update(tables: Table[_]*) = {
      new UpdateQuery(conn, tables: _*)
    }

    lazy val insert = {
      new PreInsertQuery[A](this, conn)
    }

    lazy val * = {
      new WildCardInTable(this)
    }

    def find(cond: Condition): Option[A] = {
      val query = select(columns: _*) from this where cond
      var foundModel: Option[A] = None

      query.execute((models) => {
        for (model <- models) {
          model match {
            case model: A => foundModel = Some(model)
            case _ =>
          }
        }
      })

      foundModel
    }

    def getIntColumn(propertyName: String): IntColumn[A] = {
      for {
        column <- columns;
        if (column.propertyName == propertyName)
      } {
        return column match { case numberColumn: IntColumn[A] => numberColumn case _ => null }
      }

      null
    }

    def getLongColumn(propertyName: String): LongColumn[A] = {
      for {
        column <- columns
        if (column.propertyName == propertyName)
      } {
        return column match { case numberColumn: LongColumn[A] => numberColumn case _ => null }
      }

      null
    }

    def getStringColumn(propertyName: String): StringColumn[A] = {
      for {
        column <- columns
        if (column.propertyName == propertyName)
      } {
        return column match { case stringColumn: StringColumn[A] => stringColumn case _ => null }
      }

      null
    }

    def getDateColumn(propertyName: String): DateColumn[A] = {
      for {
        column <- columns
        if (column.propertyName == propertyName)
      } {
        return column match { case dateColumn: DateColumn[A] => dateColumn case _ => null }
      }

      null
    }

    override def hashCode(): Int = {
      tableName.hashCode
    }

    override def equals(obj: Any): Boolean = {
      obj match {
        case table: Table[_] => tableName == table.tableName
        case _ => false
      }
    }
  }

  object Table {
    var simpleTableNames = Map[String, Table[_]]()

    def toSimpleTableNames(table: Table[_]) = {
      val baseName = table.tableName.substring(0, 1)
      var count = 1;
      var name = baseName

      while (simpleTableNames.contains(name)) {
        name = baseName + count
        count += 1
      }

      simpleTableNames += name -> table
      name
    }
  }
}
