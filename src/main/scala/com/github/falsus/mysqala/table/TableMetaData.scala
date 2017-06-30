package com.github.falsus.mysqala

import com.github.falsus.mysqala.nameresolver.{DefaultNameResolver, NameResolver}
import com.github.falsus.mysqala.util.Using

package table {

  import java.lang.reflect.Field
  import java.sql.Connection

  import scala.collection.mutable.ListBuffer

  class TableMetaData[A](val conn: Connection, val tableClass: Class[A], val nameResolver: NameResolver) extends Using {
    lazy val defaultResolver = new DefaultNameResolver()

    lazy val databaseName = {
      using(conn.prepareStatement("SELECT DATABASE()")) { stmt =>
        using(stmt.executeQuery()) { rs =>
          if (rs.next) rs.getString(1) else null
        }
      }
    }

    lazy val tableName = {
      nameResolver.resolveTable(tableClass) match {
        case Some(name) => name
        case None => defaultResolver.resolveTable(tableClass).get
      }
    }

    lazy val columnMetaDatas = {
      var colsBuf = ListBuffer[ColumnMetaData]()
      var columnTypes = Map[String, String]()

      using(conn.getMetaData.getColumns(databaseName.toLowerCase, null, tableName, "%")) { rs =>
        while (rs.next) {
          columnTypes += rs.getString("COLUMN_NAME") -> rs.getString("TYPE_NAME")
        }
      }

      for (field <- tableClass.getDeclaredFields) {
        nameResolver.resolveField(tableClass, field) match {
          case Some(name) => if (columnTypes.contains(name)) {
            colsBuf += getColumnMetaData(field, name, columnTypes(name))
          }
          case None =>
        }
      }

      colsBuf.toList
    }

    def getColumnMetaData(field: Field, columnName: String, columnType: String): ColumnMetaData = {
      // TODO:support all types
      columnType match {
        case "integer" => new ColumnMetaData(field.getName, columnName, field.getType, classOf[Int])
        case "timestamp" => new ColumnMetaData(field.getName, columnName, field.getType, classOf[java.sql.Timestamp])
        case "clob" => new ColumnMetaData(field.getName, columnName, field.getType, classOf[Long])
        case "varchar" => new ColumnMetaData(field.getName, columnName, field.getType, classOf[String])
      }
    }
  }

}
