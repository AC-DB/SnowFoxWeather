package top.atsuko.snowfoxweather.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import top.atsuko.snowfoxweather.R
import top.atsuko.snowfoxweather.data.remote.api.GeoApi
import top.atsuko.snowfoxweather.data.remote.interceptor.AuthInterceptor // 引入新增的拦截器
import top.atsuko.snowfoxweather.data.remote.interceptor.LogInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true // 忽略未知字段
            coerceInputValues = true // 容错处理
            encodeDefaults = true    // 序列化时包含默认值
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor) // 认证拦截器
            .addInterceptor(LogInterceptor()) // 日志拦截器
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.base_url))
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideGeoApi(retrofit: Retrofit): GeoApi {
        return retrofit.create(GeoApi::class.java)
    }
}
