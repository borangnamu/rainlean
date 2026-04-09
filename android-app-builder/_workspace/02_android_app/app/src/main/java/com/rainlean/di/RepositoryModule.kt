package com.rainlean.di

import com.rainlean.data.repository.CompositeWeatherRepository
import com.rainlean.data.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: CompositeWeatherRepository): WeatherRepository
}
