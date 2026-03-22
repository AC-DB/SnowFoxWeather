package top.atsuko.snowfoxweather.manager

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.atsuko.snowfoxweather.utils.JwtHelper
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor() {

    private var cachedToken: String? = null
    private var tokenExpiration: Long = 0
    private val mutex = Mutex()

    // 缓冲时间：过期前 60 秒触发刷新
    private val REFRESH_BUFFER_SECONDS = 60L

    /**
     * 获取有效的 Token
     * 如果缓存为空或即将过期，则自动刷新
     * 线程安全：高并发下只会生成一次
     */
    suspend fun getValidToken(): String {
        val currentTime = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()

        // 1. 快速检查：如果缓存有效且未过期，直接返回
        if (isTokenValid(currentTime)) {
            return cachedToken!!
        }

        // 2. 加锁刷新：双重检查防止并发多次生成
        mutex.withLock {
            // 再次检查 (可能在等待锁的过程中已被其他线程刷新)
            if (isTokenValid(currentTime)) {
                return cachedToken!!
            }

            // 生成新 Token
            val result = JwtHelper.generateTokenInfo()

            return if (result != null) {
                cachedToken = result.token
                tokenExpiration = result.expiresAt
                result.token
            } else {
                // 如果生成失败 (如配置错误)，返回空字符串由拦截器处理
                ""
            }
        }
    }

    private fun isTokenValid(currentTime: Long): Boolean {
        return cachedToken != null && (tokenExpiration - currentTime > REFRESH_BUFFER_SECONDS)
    }

    // 供调试或注销使用
    suspend fun clearToken() {
        mutex.withLock {
            cachedToken = null
            tokenExpiration = 0
        }
    }
}
