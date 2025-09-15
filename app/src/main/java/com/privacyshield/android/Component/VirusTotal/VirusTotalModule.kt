package com.privacyshield.android.Component.VirusTotal

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VirusTotalModule {

    @Provides
    @Singleton
    fun provideVirusTotalApi(): VirusTotalApi {
        return VirusTotalApiImpl("472d41dd279f8a5dfaf439beb1108e5e921022388fc7c087b8e74d81f28bbf9a") // yaha tumhara actual VirusTotalApi implementation
    }
}
