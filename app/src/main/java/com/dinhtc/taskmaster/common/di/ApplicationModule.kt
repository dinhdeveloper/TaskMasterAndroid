package com.dinhtc.taskmaster.common.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.dinhtc.taskmaster.BuildConfig.API_URL
import com.dinhtc.taskmaster.service.ApiHelper
import com.dinhtc.taskmaster.service.ApiHelperImpl
import com.dinhtc.taskmaster.service.ApiService
import com.dinhtc.taskmaster.service.CustomLoggingInterceptor
import com.dinhtc.taskmaster.service.GeneralHeaderInterceptor
import com.dinhtc.taskmaster.service.SpecialHeaderInterceptor
import com.dinhtc.taskmaster.utils.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.conscrypt.BuildConfig.DEBUG
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    @Provides
    fun provideBaseUrl() = "http://192.168.1.115:8080/api/" //192.168.1.6  192.168.1.110
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {

        val loggingInterceptor = CustomLoggingInterceptor()
        return if (!DEBUG) {
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(6000, TimeUnit.SECONDS)
                .writeTimeout(6000, TimeUnit.SECONDS)
                .build()
        } else OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, BASE_URL: String): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideApiHelper(apiHelper: ApiHelperImpl): ApiHelper = apiHelper

    @Provides
    @Singleton
    fun provideSharedPreferencesManager(): SharedPreferencesManager {
        return SharedPreferencesManager.instance
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("your_prefs_name", Context.MODE_PRIVATE)
    }
}