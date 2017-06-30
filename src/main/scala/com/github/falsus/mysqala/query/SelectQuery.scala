package com.github.falsus.mysqala

import com.github.falsus.mysqala.connection.ConnectionManager
import com.github.falsus.mysqala.selectable.{Column, Selectable, WildCard, WildCardInTable}
import com.github.falsus.mysqala.table.Table
import com.github.falsus.mysqala.util.Using

import scala.collection.mutable

package query {

  import scala.collection.mutable.ListBuffer

  class SelectQuery(val insertQuery: Option[InsertQuery[_]], val connManager: ConnectionManager, colsArray: Selectable*) extends WhereQuery[SelectQuery] with Using {
    override val subInstance: SelectQuery = this
    private val columns: List[Selectable] = colsArray.toList

    def freeze(): FreezedSelectQuery = {
      null
    }

    private def conn = connManager.connection

    override def build(values: ListBuffer[Any]): String = {
      insertQuery.map(_.build(values) + " ").getOrElse("") +
        "SELECT " +
        columns.map { col => col.toRawQuery }.mkString(", ") +
        " FROM " + firstFromTable.toRawQuery(values) +
        buildWhere(values) + buildOrder(values) + buildLimit(values) + buildOffset(values)
    }

    override def executeUpdate(): Int = {
      if (insertQuery.isEmpty) {
        throw new Exception("damedesu")
      }

      val values = ListBuffer[Any]()

      using(conn.prepareStatement(build(values))) { stmt =>
        var index = 1
        for (value <- values) {
          value match {
            case num: Int => stmt.setInt(index, num)
            case text: String => stmt.setString(index, text)
            case date: java.util.Date => stmt.setTimestamp(index, new java.sql.Timestamp(date.getTime))
            case _ => println("atode reigai")
          }

          index += 1
        }

        stmt.executeUpdate()
      }
    }

    def execute(f: (List[Any]) => Unit): Unit = {
      if (insertQuery.isDefined) {
        throw new Exception("damedesu")
      }

      val values = ListBuffer[Any]()
      val foundConstructors = findConstructors()

      using(conn.prepareStatement(build(values))) { stmt =>
        setValues(stmt, values, 1)

        // TODO:extract common class
        using(stmt.executeQuery()) { rs =>
          while (rs.next) {
            var params = Map[Table[_], Array[AnyRef]]()

            def findTable(dbTableName: String): Table[_] = {
              for ((table, (_, _)) <- foundConstructors) {
                if (table.tableName equalsIgnoreCase dbTableName) {
                  return table
                }
              }

              null
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

            var models = ListBuffer[Any]()

            for ((table, (constructor, _)) <- foundConstructors) {
              models += constructor.newInstance(params(table): _*)
            }

            f(models.toList)
          }
        }
      }
    }

    def findConstructors(): Map[Table[_], (java.lang.reflect.Constructor[_], List[Column[_, _]])] = {
      var selectingModels = mutable.LinkedHashMap[Table[_], ListBuffer[Column[_, _]]]()

      def addSelectingColumn(table: Table[_], columns: Column[_, _]*) {
        if (!selectingModels.contains(table)) {
          selectingModels += table -> ListBuffer[Column[_, _]]()
        }

        for (column <- columns) {
          if (!selectingModels(table).contains(column)) {
            selectingModels(table) += column
          }
        }
      }

      for (column <- columns) {
        column match {
          case col: Column[_, _] => addSelectingColumn(col.parent, col)
          case wildCardInTable: WildCardInTable => addSelectingColumn(wildCardInTable.table, wildCardInTable.table.columns: _*)
          case _: WildCard =>
            var fromTable: Option[FromTable[_]] = Some(firstFromTable)

            while (fromTable.isDefined) {
              addSelectingColumn(fromTable.get.table, fromTable.get.table.columns: _*)
              fromTable = fromTable.get.nextTable
            }
        }
      }

      var foundConstructors = mutable.LinkedHashMap[Table[_], (java.lang.reflect.Constructor[_], List[Column[_, _]])]()
      val classPool = javassist.ClassPool.getDefault

      classPool.insertClassPath(new javassist.ClassClassPath(this.getClass))

      for ((table, columns) <- selectingModels) {
        val c = classPool.get(table.tableClass.getName)

        for {
          m <- c.getConstructors
          if columns.length == m.getParameterTypes.length
          code = m.getMethodInfo.getAttribute("Code").asInstanceOf[javassist.bytecode.CodeAttribute]
          lval = code.getAttribute("LocalVariableTable").asInstanceOf[javassist.bytecode.LocalVariableAttribute]
          if lval != null
        } {
          val types = m.getParameterTypes

          val paramNames = for {
            i <- 0 until types.length
            name = lval.getConstPool.getUtf8Info(lval.nameIndex(i + 1))
          } yield name

          var matched = true

          for (column <- columns) {
            if (!paramNames.contains(column.propertyName)) {
              matched = false
            }
          }

          if (matched) {
            var paramTypes = ListBuffer[Class[_]]()
            var columnsForConstructor = ListBuffer[Column[_, _]]()

            for {
              i <- 0 until types.length
              name = lval.getConstPool.getUtf8Info(lval.nameIndex(i + 1))
            } {
              columns.find(_.propertyName == name) match {
                case Some(column) =>
                  columnsForConstructor += column
                  paramTypes += (types(i).getName match {
                    case "int" => classOf[Int]
                    case "long" => classOf[Long]
                    case typeName: String => Class.forName(typeName)
                  })
                case _ =>
                  println("not found " + name)
              }
            }

            foundConstructors += table -> ((table.tableClass.getConstructor(paramTypes.toArray: _*), columnsForConstructor.toList))
          }
        }

      }

      foundConstructors.toMap
    }
  }

}
