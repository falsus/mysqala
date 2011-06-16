package com.github.falsus.mysqala

import table.Table

package selectable {
  class WildCardInTable(val table: Table[_]) extends Selectable {
    def toRawQuery = {
      table.shortDatabaseTableName + ".*"
    }
  }
}
