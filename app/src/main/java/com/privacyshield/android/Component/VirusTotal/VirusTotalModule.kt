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
        return VirusTotalApiImpl("458fae720537c3b106bd159c1e650373b8f02a7c838aa8bacc5defc8d59614ef") // yaha tumhara actual VirusTotalApi implementation
    }
}
