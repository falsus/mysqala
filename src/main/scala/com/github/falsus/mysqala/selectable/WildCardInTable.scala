package com.github.falsus.mysqala

import com.github.falsus.mysqala.table.Table

package selectable {

  class WildCardInTable(val table: Table[_]) extends Selectable {
    def toRawQuery = table.shortDatabaseTableName + ".*"
  }

}
