package com.github.falsus.mysqala

import connection.ConnectionManager
import table.Table
import selectable.Column
import condition.PlaceHolder
import querystring.{ QueryString, DynamicQueryString, FixedQueryString }
import util.Using

package query {
  import scala.collection.mutable.ListBuffer

  class FreezedSelectQuery(val freezedValues: List[_], val queryStrings: List[QueryString], val foundConstructors: Map[Table[_], Tuple2[java.lang.reflect.Constructor[_], List[Column[_, _]]]], val connManager: ConnectionManager) extends Using {
    def mergeValues(dynamicValues: Seq[_]) = {
      val values = ListBuffer[Any]()
      var current = 0

      for (freezedValue <- freezedValues) {
        freezedValue match {
          case p: PlaceHolder =>
            values.append(dynamicValues(current))
            current += 1
          case _ => values.append(freezedValue)
        }
      }

      values
    }

    def setValues(stmt: java.sql.PreparedStatement, values: Seq[_], index: Int): Int = {
      var currentIndex = index

      for (value <- values) {
        value match {
          case num: Int => stmt.setInt(currentIndex, num)
          case text: String => stmt.setString(currentIndex, text)
          case date: java.util.Date => stmt.setTimestamp(currentIndex, new java.sql.Timestamp(date.getTime))
          case seq: Seq[_] => currentIndex = setValues(stmt, seq, currentIndex) - 1
          case _ => println("atode reigai")
        }

        currentIndex += 1
      }

      currentIndex
    }

    def execute(dynamicValues: Any*)(f: (List[_]) => Unit) = {
      val rawQuery = new StringBuilder()
      val values = mergeValues(dynamicValues)
      val valuesTmp = values.clone

      for (queryString <- queryStrings) {
        queryString match {
          case d: DynamicQueryString => d.build(rawQuery, valuesTmp)
          case f: FixedQueryString => rawQuery.append(f.rawString)
        }
      }

      using(connManager.connection.prepareStatement(rawQuery.toString)) { stmt =>
        setValues(stmt, values, 1)

        // TODO:extract common class
        using(stmt.executeQuery()) { rs =>
          while (rs.next) {
            var params = Map[Table[_], Array[AnyRef]]()

            def findTable(dbTableName: String): Table[_] = {
              for ((table, (constructor, columns)) <- foundConstructors) {
                if (table.tableName equalsIgnoreCase dbTableName) {
                  return table
                }
              }

              null
            }

            val metaData = rs.getMetaData
            for (i <- 1 to metaData.getColumnCount) {
              val table = findTable(metaData.getTableName(i))
              val (constructor, columns) = foundConstructors(table)

              if (!params.contains(table)) {
                params += table -> new Array[AnyRef](columns.length)
              }

              val column = columns.find(_.databaseName equalsIgnoreCase metaData.getColumnName(i)).get
              params(table)(columns.indexOf(column)) = column.toField(rs, i)
            }

            var models = ListBuffer[Any]()

            for ((table, (constructor, columns)) <- foundConstructors) {
              models += constructor.newInstance(params(table): _*)
            }

            f(models.toList)
          }
        }
      }
    }
  }
}