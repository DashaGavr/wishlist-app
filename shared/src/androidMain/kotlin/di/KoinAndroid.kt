package di

import android.content.Context
import db.buildDatabase
import db.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun initKoin(ctx: Context) {
    startKoin {
        androidContext(ctx)
        modules(
            androidDatabaseModule(ctx),
            appModule
        )
    }
}

private fun androidDatabaseModule(ctx: Context) = org.koin.dsl.module {
    single { getDatabaseBuilder(ctx).buildDatabase() }
}
