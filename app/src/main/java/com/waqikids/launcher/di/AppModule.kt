package com.waqikids.launcher.di

import android.content.Context
import com.waqikids.launcher.data.local.PreferencesManager
import com.waqikids.launcher.data.repository.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
    
    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        preferencesManager: PreferencesManager
    ): AppRepository {
        return AppRepository(context, preferencesManager)
    }
}
