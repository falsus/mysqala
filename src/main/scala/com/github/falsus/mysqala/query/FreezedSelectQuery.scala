package com.github.falsus.mysqala

import com.github.falsus.mysqala.condition.PlaceHolder
import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.querystring.{DynamicQueryString, FixedQueryString, QueryString}
import com.github.falsus.mysqala.selectable.Column
import com.github.falsus.mysqala.table.Table
import com.github.falsus.mysqala.util.Using

package query {

  import scala.collection.mutable.ListBuffer

  class FreezedSelectQuery(val freezedValues: List[_], val queryStrings: List[QueryString], val foundConstructors: Map[Table[_], (java.lang.reflect.Constructor[_], List[Column[_, _]])], val connManager: ConnectionManager) extends Using {
    def mergeValues(dynamicValues: Seq[_]): ListBuffer[_] = {
      val values = ListBuffer[Any]()
      var current = 0

      for (freezedValue <- freezedValues) {
        freezedValue match {
          case _: PlaceHolder =>
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

    def execute(dynamicValues: Any*)(f: (Iterable[_]) => Unit): Unit = {
      val values = mergeValues(dynamicValues)
      val valuesTmp = values.clone

      val query = queryStrings.collect {
        case d: DynamicQueryString => d.build(valuesTmp)
        case f: FixedQueryString => f.rawString
      }.mkString

      using(connManager.connection.prepareStatement(query)) { stmt =>
        setValues(stmt, values, 1)

        // TODO:extract common class
        using(stmt.executeQuery()) { rs =>
          while (rs.next) {
            var params = Map[Table[_], Array[AnyRef]]()

            def findTable(dbTableName: String): Table[_] = {
              foundConstructors.find {
                case (table, (_, _)) => table.tableName equalsIgnoreCase dbTableName
              } match {
                case Some((table, (_, _))) => table
                case None => null
              }
            }

            val metaData = rs.getMetaData
            for (i <- 1 to metaData.getColumnCount) {
              val table = findTable(metaData.getTableName(i))
              val (_, columns) = foundConstructors(table)

              if (!params.contains(table)) {
                params += table -> new Array[AnyRef](columns.length)
              }

              val column = columns.find(_.databaseName equalsIgnoreCase metaData.getColumnName(i)).get
              params(table)(columns.indexOf(column)) = column.toField(rs, i)
            }

            val models = for {
              (table, (constructor, _)) <- foundConstructors
              model = constructor.newInstance(params(table): _*)
            } yield model

            f(models)
          }
        }
      }
    }
  }

}