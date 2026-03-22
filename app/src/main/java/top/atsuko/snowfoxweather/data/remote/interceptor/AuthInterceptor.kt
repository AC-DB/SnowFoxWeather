package top.atsuko.snowfoxweather.data.remote.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import top.atsuko.snowfoxweather.manager.TokenManager
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 使用 runBlocking 获取 Token (TokenManager 内部处理了缓存和并发)
        val token = runBlocking {
            tokenManager.getValidToken()
        }

        // 如果获取失败，直接放行（让后端返回 401，或者在这里抛出异常）
        if (token.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}
