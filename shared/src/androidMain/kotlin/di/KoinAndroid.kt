package di

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.Settings
import db.buildDatabase
import db.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(ctx: Context, vararg extraModules: Module) {
    startKoin {
        androidContext(ctx)
        modules(
            androidPlatformModule(ctx),
            appModule,
            *extraModules
        )
    }
}

private fun androidPlatformModule(ctx: Context) = org.koin.dsl.module {
    single { getDatabaseBuilder(ctx).buildDatabase() }
    single<Settings> {
        SharedPreferencesSettings(ctx.getSharedPreferences("app_settings", Context.MODE_PRIVATE))
    }
}