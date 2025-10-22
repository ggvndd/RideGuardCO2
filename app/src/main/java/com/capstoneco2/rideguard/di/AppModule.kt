package com.capstoneco2.rideguard.di

import com.capstoneco2.rideguard.service.FCMTokenService
import com.capstoneco2.rideguard.service.DeviceUserAccountService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideFCMTokenService(firestore: FirebaseFirestore): FCMTokenService {
        return FCMTokenService(firestore)
    }

    @Provides
    @Singleton
    fun provideDeviceUserAccountService(fcmTokenService: FCMTokenService): DeviceUserAccountService {
        return DeviceUserAccountService(fcmTokenService)
    }
}