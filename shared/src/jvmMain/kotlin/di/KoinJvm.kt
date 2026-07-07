package di

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import db.buildDatabase
import db.getDatabaseBuilder
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.prefs.Preferences

fun initKoin(vararg extraModules: Module) {
    startKoin {
        modules(
            module {
                single { getDatabaseBuilder().buildDatabase() }
                single<Settings> { PreferencesSettings(Preferences.userRoot().node("wishlist_app")) }
            },
            appModule,
            *extraModules
        )
    }
}