package org.example.project.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import presentation.AiChatViewModel
import presentation.SettingsViewModel
import presentation.WishDetailViewModel
import presentation.WishlistDetailViewModel
import presentation.WishlistViewModel

val viewModelModule = module {
    viewModel { WishlistViewModel(get()) }
    viewModel { params -> WishlistDetailViewModel(get(), get(), params.get()) }
    viewModel { params -> WishDetailViewModel(get(), params.get()) }
    viewModel { AiChatViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
