package com.github.falsus.mysqala

import util.Using
import selectable.{ Selectable, Column, WildCardInTable, LastInsertId, IntColumn, StringColumn, LongColumn, DateColumn }
import nameresolver.{ NameResolver, DefaultNameResolver }

package table {
  import java.sql.Connection
  import java.lang.reflect.Field
  import scala.collection.mutable.ListBuffer

  class MetaData[A](val table: Table[A], val conn: Connection, val tableClass: Class[A], val nameResolver: NameResolver) extends Using {
    lazy val defaultResolver = new DefaultNameResolver()

    lazy val databaseName = {
      using(conn.prepareStatement("SELECT DATABASE()")) { stmt =>
        using(stmt.executeQuery()) { rs =>
          if (rs.next) {
            rs.getString(1)
          } else {
            null
          }
        }
      }
    }

    lazy val tableName = {
      nameResolver.resolveTable(tableClass) match {
        case Some(name) => name
        case None => defaultResolver.resolveTable(tableClass).get
      }
    }

    lazy val columns = {
      var colsBuf = ListBuffer[Column[A, _]]()
      var columnTypes = Map[String, String]()

      using(conn.getMetaData.getColumns(databaseName.toLowerCase, null, tableName, "%")) { rs =>
        while (rs.next) {
          columnTypes += rs.getString("COLUMN_NAME") -> rs.getString("TYPE_NAME")
        }
      }

      for (field <- tableClass.getDeclaredFields) {
        nameResolver.resolveField(tableClass, field) match {
          case Some(name) => colsBuf += getColumn(field, name, columnTypes(name))
          case None =>
        }
      }

      colsBuf.toList
    }

    def getColumn(field: Field, columnName: String, columnType: String): Column[A, _] = {
      // TODO:support all types
      columnType match {
        case "integer" => new IntColumn(table, field.getName, columnName, field.getType, classOf[Int])
        case "timestamp" => new DateColumn(table, field.getName, columnName, field.getType, classOf[java.sql.Timestamp])
        case "clob" => new LongColumn(table, field.getName, columnName, field.getType, classOf[Long])
        case "varchar" => new StringColumn(table, field.getName, columnName, field.getType, classOf[String])
      }
    }
  }

  object MetaData {
    def getMetaDatas[A](table: Table[A], conn: Connection, tableClass: Class[A], nameResolver: NameResolver) = {
      val metaData = new MetaData(table, conn, tableClass, nameResolver)

      (metaData.databaseName, metaData.tableName, metaData.columns)
    }
  }
}
