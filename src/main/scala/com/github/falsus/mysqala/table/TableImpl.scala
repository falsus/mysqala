package com.github.falsus.mysqala

import connection.ConnectionManager
import nameresolver.{ NameResolver, DefaultNameResolver }
import query.{ SelectQuery, PreInsertQuery, DeleteQuery, UpdateQuery, SelectLastInsertIdQuery }
import condition.Condition
import selectable.{ Selectable, Column, WildCardInTable, LastInsertId, IntColumn, StringColumn, LongColumn, DateColumn, OrderedColumn }

package table {
  import java.sql.Timestamp

  class TableImpl[A](val connManager: ConnectionManager, val tableClass: Class[A], val databaseName: String,
    val tableName: String, val shortDatabaseTableName: String, val columnMetaDatas: List[ColumnMetaData]) extends Table[A] {
    lazy val toRawQuery = tableName + " " + shortDatabaseTableName
    lazy val toRawQuerySingle = tableName
    lazy val columns = {
      for {
        columnMetaData <- columnMetaDatas
        column = createColumn(columnMetaData)
      } yield column
    }

    private def conn = connManager.connection

    implicit def columnToOrderedColumn(col: Column[_, _]): OrderedColumn = new OrderedColumn(col, true)

    def createColumn(columnMetaData: ColumnMetaData): Column[A, _] = {
      columnMetaData.columnClass match {
        case c if c == classOf[Int] => new IntColumn(this, columnMetaData.fieldName, columnMetaData.columnName, columnMetaData.fieldClass, columnMetaData.columnClass)
        case c if c == classOf[Timestamp] => new DateColumn(this, columnMetaData.fieldName, columnMetaData.columnName, columnMetaData.fieldClass, columnMetaData.columnClass)
        case c if c == classOf[Long] => new LongColumn(this, columnMetaData.fieldName, columnMetaData.columnName, columnMetaData.fieldClass, columnMetaData.columnClass)
        case c if c == classOf[String] => new StringColumn(this, columnMetaData.fieldName, columnMetaData.columnName, columnMetaData.fieldClass, columnMetaData.columnClass)
      }
    }

    def this(connectionManager: ConnectionManager, tableClass: Class[A], metaData: TableMetaData[A]) = {
      this(connectionManager, tableClass, metaData.databaseName, metaData.tableName, Table.toSimpleTableName(metaData.tableName), metaData.columnMetaDatas)
    }

    def this(connectionManager: ConnectionManager, tableClass: Class[A], nameResolver: NameResolver = new DefaultNameResolver()) = {
      this(connectionManager, tableClass, new TableMetaData(connectionManager.connection, tableClass, nameResolver))
    }

    def select(columns: Selectable*) = {
      columns match {
        case Seq(column: LastInsertId) => new SelectLastInsertIdQuery(connManager, columns: _*)
        case _ => new SelectQuery(None, connManager, columns: _*)
      }
    }

    def last_insert_id() = new LastInsertId()
    def delete = new DeleteQuery(connManager)
    def update(tables: Table[_]*) = new UpdateQuery(connManager, tables: _*)
    def insert = new PreInsertQuery[A](this, connManager)
    def * = new WildCardInTable(this)

    def find(cond: Condition): Option[A] = {
      val query = select(columns: _*) from this where cond
      var foundModel: Option[A] = None

      query.execute { (models) =>
        {
          for (model <- models) {
            model match {
              case model: A => foundModel = Some(model)
              case _ =>
            }
          }
        }
      }

      foundModel
    }

    def getIntColumn(propertyName: String): IntColumn[A] = {
      for {
        column <- columns
        if (column.propertyName == propertyName)
      } {
        return column match { case numberColumn: IntColumn[A] => numberColumn case _ => throw new ColumnTypeException() }
      }

      throw new ColumnNotExistException()
    }

    def getLongColumn(propertyName: String): LongColumn[A] = {
      for {
        column <- columns
        if (column.propertyName == propertyName)
      } {
        return column match { case numberColumn: LongColumn[A] => numberColumn case _ => throw new ColumnTypeException() }
      }

      throw new ColumnNotExistException()
    }

    def getStringColumn(propertyName: String): StringColumn[A] = {
      for {
        column <- columns
        if (column.propertyName == propertyName)
      } {
        return column match { case stringColumn: StringColumn[A] => stringColumn case _ => throw new ColumnTypeException() }
      }

      throw new ColumnNotExistException()
    }

    def getDateColumn(propertyName: String): DateColumn[A] = {
      for {
        column <- columns
        if (column.propertyName == propertyName)
      } {
        return column match { case dateColumn: DateColumn[A] => dateColumn case _ => throw new ColumnTypeException() }
      }

      throw new ColumnNotExistException()
    }

    def cloneForInnerJoin: Table[A] = {
      new TableImpl(this.connManager, this.tableClass, this.databaseName, this.tableName, Table.toSimpleTableName(this.tableName), columnMetaDatas)
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

  class ColumnNotExistException extends Exception
  class ColumnTypeException extends Exception
}