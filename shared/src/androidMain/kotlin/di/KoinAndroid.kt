package di

import android.content.Context
import db.buildDatabase
import db.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(ctx: Context, vararg extraModules: Module) {
    startKoin {
        androidContext(ctx)
        modules(
            androidDatabaseModule(ctx),
            appModule,
            *extraModules
        )
    }
}

private fun androidDatabaseModule(ctx: Context) = org.koin.dsl.module {
    single { getDatabaseBuilder(ctx).buildDatabase() }
}