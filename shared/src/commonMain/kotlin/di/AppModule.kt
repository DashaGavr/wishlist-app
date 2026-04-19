package di

import data.WishlistRepository
import data.WishRepository
import db.AppDatabase
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import presentation.WishDetailViewModel
import presentation.WishlistDetailViewModel
import presentation.WishlistViewModel

val appModule: Module = module {
    // DAOs — производные от AppDatabase, которая регистрируется платформой
    single { get<AppDatabase>().wishlistDao() }
    single { get<AppDatabase>().wishDao() }

    // Repositories
    single { WishlistRepository(get()) }
    single { WishRepository(get()) }

    // ViewModels
    factoryOf(::WishlistViewModel)
    factory { params -> WishlistDetailViewModel(get(), get(), params.get()) }
    factory { params -> WishDetailViewModel(get(), params.get()) }
}
