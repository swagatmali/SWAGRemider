package com.swagatmali.remider.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.swagatmali.remider.db.ReminderDatabase

/** Android driver: SQLite through AndroidSqliteDriver, backed by a Context. */
class AndroidDatabaseDriverFactory(
    private val context: Context,
) : DatabaseDriverFactory {
    override fun create(): SqlDriver =
        AndroidSqliteDriver(
            schema = ReminderDatabase.Schema,
            context = context,
            name = "reminder.db",
        )
}
