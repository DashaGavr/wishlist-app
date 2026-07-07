package di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import db.buildDatabase
import db.getDatabaseBuilder
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

fun initKoin(vararg extraModules: Module) {
    startKoin {
        modules(
            module {
                single { getDatabaseBuilder().buildDatabase() }
                single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
            },
            appModule,
            *extraModules
        )
    }
}
