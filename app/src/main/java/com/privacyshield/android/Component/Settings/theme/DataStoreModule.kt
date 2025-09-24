package com.privacyshield.android.Component.Settings.theme


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppSettingsSerializer(): Serializer<AppSettings> = AppSettingsSerializer()

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context,
        serializer: Serializer<AppSettings>
    ): DataStore<AppSettings> =
        DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.dataStoreFile("user_preferences.pb") },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
}