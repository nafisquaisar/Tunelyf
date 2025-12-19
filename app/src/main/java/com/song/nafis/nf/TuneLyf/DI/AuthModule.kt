package com.song.nafis.nf.TuneLyf.DI

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.song.nafis.nf.TuneLyf.Auth.AuthRepository
import com.song.nafis.nf.TuneLyf.Auth.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        @ApplicationContext context: Context
    ): AuthRepository {
        return FirebaseAuthRepository(firebaseAuth, context)
    }
}
