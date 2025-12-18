package com.song.nafis.nf.TuneLyf.DI

import android.content.Context
import com.song.nafis.nf.TuneLyf.Api.AudiusApi
import com.song.nafis.nf.TuneLyf.Repository.PlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("Audius")
    fun provideRetrofit(@Named("AudiusClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://tunelyfmusic.onrender.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("AudiusClient")
    fun provideAudiusOkHttpClient(): OkHttpClient = provideOkHttpClient()

    @Provides
    @Singleton
    fun provideAudiusApi(@Named("Audius") retrofit: Retrofit): AudiusApi {
        return retrofit.create(AudiusApi::class.java)
    }

//    @Provides
//    @Singleton
//    fun providePlayerRepository(
//        @ApplicationContext context: Context,
//        audiusApi: AudiusApi
//    ): PlayerRepository {
//        return PlayerRepository(context, audiusApi)
//    }
}
