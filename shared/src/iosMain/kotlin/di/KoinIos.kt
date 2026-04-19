package di

import db.buildDatabase
import db.getDatabaseBuilder
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin() {
    startKoin {
        modules(
            module { single { getDatabaseBuilder().buildDatabase() } },
            appModule
        )
    }
}
