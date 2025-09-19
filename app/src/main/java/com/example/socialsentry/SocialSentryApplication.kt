package com.example.socialsentry

import android.app.Application
import com.example.socialsentry.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SocialSentryApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@SocialSentryApplication)
            modules(appModule)
        }
    }
}

