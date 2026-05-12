package di

import data.WishlistRepository
import data.WishRepository
import db.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    // DAOs — производные от AppDatabase, которая регистрируется платформой
    single { get<AppDatabase>().wishlistDao() }
    single { get<AppDatabase>().wishDao() }

    // Repositories
    single { WishlistRepository(get()) }
    single { WishRepository(get()) }
}
