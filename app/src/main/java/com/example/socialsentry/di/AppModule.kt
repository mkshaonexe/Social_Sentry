package com.example.socialsentry.di

import com.example.socialsentry.data.datastore.SocialSentryDataStore
import com.example.socialsentry.presentation.viewmodel.SocialSentryViewModel
import com.example.socialsentry.presentation.viewmodel.WorkoutViewModel
import com.example.socialsentry.service.WorkoutSessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // DataStore
    single { SocialSentryDataStore(androidContext()) }
    
    // Services
    single { WorkoutSessionManager(androidContext()) }
    
    // ViewModels
    viewModel { SocialSentryViewModel(get()) }
    viewModel { WorkoutViewModel(get()) }
}

