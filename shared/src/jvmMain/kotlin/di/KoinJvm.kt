package di

import db.buildDatabase
import db.getDatabaseBuilder
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

fun initKoin(vararg extraModules: Module) {
    startKoin {
        modules(
            module { single { getDatabaseBuilder().buildDatabase() } },
            appModule,
            *extraModules
        )
    }
}