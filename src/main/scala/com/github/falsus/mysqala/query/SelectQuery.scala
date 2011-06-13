package com.github.falsus.mysqala

import selectable.{ Selectable, Column, WildCard, WildCardInTable }
import condition.Condition
import table.Table
import util.Using

package query {
  import java.sql.Connection
  import scala.collection.mutable.{ ListBuffer, LinkedHashMap }

  class SelectQuery(val insertQuery: Option[InsertQuery[_]], val conn: Connection, colsArray: Selectable*) extends Query with Using {
    var firstFromTable: FromTable[_] = null
    var lastJoinTable: FromTable[_] = null
    val columns: List[Selectable] = colsArray.toList

    class FromTable[A](val table: Table[A], val on: Option[Condition] = None) {
      var next: Option[FromTable[_]] = None

      def nextTable = next

      def join[B](joinTable: FromTable[B]) = {
        next = Some(joinTable)
        joinTable
      }

      def toRawQuery(builder: StringBuilder, values: ListBuffer[Any]) {
        if (on != None) {
          builder.append(" JOIN ")
        }

        builder.append(table.toRawQuery)

        on match {
          case Some(cond) =>
            builder.append(" ON ")
            cond.toRawQuery(builder, values)
          case _ =>
        }

        next match { case Some(nextFromTable) => nextFromTable.toRawQuery(builder, values) case _ => }
      }
    }

    def from[A](table: Table[A]) = {
      firstFromTable = new FromTable(table)
      lastJoinTable = firstFromTable
      this
    }

    def join[A](table: Table[A], on: Condition) = {
      lastJoinTable = lastJoinTable.join(new FromTable(table, Some(on)))

      this
    }

    override def build(rawQuery: StringBuilder, values: ListBuffer[Any]) = {
      rawQuery.append("SELECT ")

      var first = true

      for (column <- columns) {
        if (first) {
          first = false
        } else {
          rawQuery.append(", ")
        }

        rawQuery.append(column.toRawQuery)
      }

      rawQuery.append(" FROM ")
      firstFromTable.toRawQuery(rawQuery, values)

      if (firstWhereCondition != null) {
        var first = true

        for ((conditoin, and) <- whereConditions) {
          if (first) {
            rawQuery.append(" WHERE ")
            first = false
          } else if (and) {
            rawQuery.append(" AND ")
          } else {
            rawQuery.append(" OR ")
          }

          rawQuery.append("(")
          conditoin.toRawQuery(rawQuery, values)
          rawQuery.append(")")
        }
      }

      if (orderedColumns != null) {
        var first = true

        for (orderedColumn <- orderedColumns) {
          if (first) {
            rawQuery.append(" ORDER BY ")
            first = false
          } else {
            rawQuery.append(", ")
          }

          rawQuery.append(orderedColumn.column.toRawQuery)

          if (!orderedColumn.asc) {
            rawQuery.append(" DESC")
          }
        }

        if (limitOption != None) {
          rawQuery.append(" LIMIT ?")
          values += limitOption.get
        }

        if (offsetOption != None) {
          rawQuery.append(" OFFSET ?")
          values += offsetOption.get
        }
      }
    }

    override def executeUpdate() = {
      if (insertQuery == None) {
        throw new Exception("damedesu")
      }

      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()

      insertQuery.get.build(rawQuery, values)
      rawQuery.append(" ")

      build(rawQuery, values)

      using(conn.prepareStatement(rawQuery.toString)) { stmt =>
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

    override def execute(f: (List[Any]) => Unit) = {
      if (insertQuery != None) {
        throw new Exception("damedesu")
      }

      var values = ListBuffer[Any]()
      val rawQuery = new StringBuilder()
      var selectingModels = LinkedHashMap[Table[_], ListBuffer[Column[_, _]]]()

      build(rawQuery, values)

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
          case wildCard: WildCard =>
            var fromTable: Option[FromTable[_]] = Some(firstFromTable)

            while (fromTable != None) {
              addSelectingColumn(fromTable.get.table, fromTable.get.table.columns: _*)
              fromTable = fromTable.get.nextTable
            }
        }
      }

      var foundConstructors = LinkedHashMap[Table[_], Tuple2[java.lang.reflect.Constructor[_], List[Column[_, _]]]]()

      val classPool = javassist.ClassPool.getDefault

      classPool.insertClassPath(new javassist.ClassClassPath(this.getClass()))

      for ((table, columns) <- selectingModels) {
        val c = classPool.get(table.tableClass.getName)

        for {
          m <- c.getConstructors()
          if columns.length == m.getParameterTypes.length
          code = m.getMethodInfo().getAttribute("Code").asInstanceOf[javassist.bytecode.CodeAttribute]
          lval = code.getAttribute("LocalVariableTable").asInstanceOf[javassist.bytecode.LocalVariableAttribute]
          if lval != null
        } {
          val types = m.getParameterTypes

          val paramNames = for {
            i <- 0 until types.length
            name = lval.getConstPool().getUtf8Info(lval.nameIndex(i + 1))
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
              name = lval.getConstPool().getUtf8Info(lval.nameIndex(i + 1))
            } {
              columns.find(_.propertyName == name) match {
                case Some(column) =>
                  columnsForConstructor += column
                  paramTypes += (types(i).getName match { case "int" => classOf[Int] case "long" => classOf[Long] case typeName: String => Class.forName(typeName) })
                case _ =>
                  println("not found " + name)
              }
            }

            foundConstructors += table -> ((table.tableClass.getConstructor(paramTypes.toArray: _*), columnsForConstructor.toList))
          }
        }
      }

      using(conn.prepareStatement(rawQuery.toString)) { stmt =>
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
