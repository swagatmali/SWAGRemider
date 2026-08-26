package com.swagatmali.remider.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.swagatmali.remider.db.ReminderDatabase

/** iOS driver: SQLite through NativeSqliteDriver (no Context needed). */
class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun create(): SqlDriver =
        NativeSqliteDriver(
            schema = ReminderDatabase.Schema,
            name = "reminder.db",
        )
}
