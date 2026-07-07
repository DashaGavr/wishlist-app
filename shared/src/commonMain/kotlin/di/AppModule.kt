package di

import data.AiChatRepository
import data.ClaudeApiService
import data.SettingsRepository
import data.WishlistRepository
import data.WishRepository
import db.AppDatabase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    // DAOs — производные от AppDatabase, которая регистрируется платформой
    single { get<AppDatabase>().wishlistDao() }
    single { get<AppDatabase>().wishDao() }

    // Repositories
    single { WishlistRepository(get()) }
    single { WishRepository(get()) }

    // Settings — инстанс Settings регистрируется платформенным модулем
    single { SettingsRepository(get()) }

    // HTTP + AI (движок Ktor подтягивается с classpath платформы: okhttp/java/darwin)
    single {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
            }
        }
    }
    single { ClaudeApiService(get()) }
    single { AiChatRepository(get()) }
}