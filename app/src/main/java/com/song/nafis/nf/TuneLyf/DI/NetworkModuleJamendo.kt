package com.song.nafis.nf.TuneLyf.DI

import com.song.nafis.nf.TuneLyf.Api.JamendoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModuleJamendo {
    private const val BASE_URL = "https://api.jamendo.com/v3.0/"

    @Provides
    @Singleton
    @Named("Jamendo")
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideJamendoApiService(@Named("Jamendo") retrofit: Retrofit): JamendoApiService =
        retrofit.create(JamendoApiService::class.java)
}
