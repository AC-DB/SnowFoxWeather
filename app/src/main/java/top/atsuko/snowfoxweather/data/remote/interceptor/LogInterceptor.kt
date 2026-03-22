package top.atsuko.snowfoxweather.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import top.atsuko.snowfoxweather.BuildConfig
import java.nio.charset.Charset

class LogInterceptor : Interceptor {

    companion object {
        private const val TAG = "NetworkLog"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 核心修改：如果是 Release 模式，直接执行请求，跳过所有日志逻辑
        // 这允许 R8/ProGuard 在编译时完全移除未使用的代码分支，优化性能
        if (!BuildConfig.DEBUG) {
            return chain.proceed(request)
        }

        // ================= 以下逻辑仅在 Debug 模式下运行 =================

        // 记录请求信息
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "Request URL: ${request.url}")
        Log.d(TAG, "Request Method: ${request.method}")
        // 注意：即使是 Debug 模式，也建议过滤掉 Authorization 等敏感 Header
        Log.d(TAG, "Request Headers: ${request.headers}")

        // 记录请求体
        try {
            request.body?.let { requestBody ->
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                Log.d(TAG, "Request Body: ${buffer.readUtf8()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read request body", e)
        }

        // 执行请求
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val endTime = System.currentTimeMillis()

        // 记录响应信息
        Log.d(TAG, "Request Duration: ${endTime - startTime}ms")
        Log.d(TAG, "Response Code: ${response.code}")

        // 记录响应体（注意大文件下载等情况可能导致 OOM，建议增加长度限制判断）
        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source?.buffer

        val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8")) ?: ""
        Log.d(TAG, "Response Body: $responseBodyString")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // 重新创建响应体（因为原 body 已被读取）
        val newResponseBody = responseBodyString.toResponseBody(responseBody?.contentType())
        return response.newBuilder()
            .body(newResponseBody)
            .build()
    }
}
