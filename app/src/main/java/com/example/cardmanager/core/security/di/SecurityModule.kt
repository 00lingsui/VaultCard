package com.example.cardmanager.core.security.di

import com.example.cardmanager.core.security.BiometricAuthManager
import com.example.cardmanager.core.security.BiometricAuthManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindBiometricAuthManager(
        impl: BiometricAuthManagerImpl
    ): BiometricAuthManager
}
