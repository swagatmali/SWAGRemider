package com.swagatmali.remider.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform bridge that constructs the SQLDelight [SqlDriver]. The concrete
 * factory is supplied per target through Koin's `platformModule()`
 * (Android needs a Context; iOS does not).
 */
interface DatabaseDriverFactory {
    fun create(): SqlDriver
}
