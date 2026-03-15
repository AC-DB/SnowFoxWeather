package top.atsuko.snowfoxweather.utils

import com.google.gson.Gson
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Security
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import top.atsuko.snowfoxweather.BuildConfig

object JwtHelper {

    // 初始化 BouncyCastle 提供者以支持 EdDSA (兼容 Android < 13)
    init {
        // 防止重复添加
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            Security.addProvider(BouncyCastleProvider())
        }
    }

    private val gson = Gson()

    /**
     * 生成并签名的 JWT Token
     * @param privateKeyContent 你的私钥字符串
     * @param keyId 对应 YOUR_KEY_ID
     * @param projectId 对应 YOUR_PROJECT_ID (sub)
     * @return 完整的 JWT 字符串
     */
    fun generateToken(
        privateKeyContent: String = BuildConfig.WEATHER_PRIVATE_KEY,
        keyId: String = BuildConfig.WEATHER_KEY_ID,
        projectId: String = BuildConfig.WEATHER_PROJECT_ID
    ): String {
        try {
            // 1. 处理私钥字符串
            val cleanKey = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "") // 去除所有空白字符

            val privateKeyBytes = Base64.getDecoder().decode(cleanKey)
            val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)

            // 使用 BouncyCastle (BC) 提供者获取 EdDSA
            val keyFactory = KeyFactory.getInstance("EdDSA", "BC")
            val privateKey: PrivateKey = keyFactory.generatePrivate(keySpec)

            // 2. 准备 Header
            val headerMap = mapOf(
                "alg" to "EdDSA",
                "kid" to keyId
            )
            val headerJson = gson.toJson(headerMap)

            // 3. 准备 Payload
            val iat = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() - 30
            val exp = iat + 900 // 15分钟有效期

            val payloadMap = mapOf(
                "sub" to projectId,
                "iat" to iat,
                "exp" to exp
            )
            val payloadJson = gson.toJson(payloadMap)

            // 4. Base64Url 编码
            val headerEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.toByteArray())
            val payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.toByteArray())

            val data = "$headerEncoded.$payloadEncoded"

            // 5. 签名
            val signer = Signature.getInstance("EdDSA", "BC")
            signer.initSign(privateKey)
            signer.update(data.toByteArray())
            val signature = signer.sign()

            val signatureEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(signature)

            // 6. 拼接
            return "$data.$signatureEncoded"

        } catch (e: Exception) {
            e.printStackTrace()
            return "" // 或者抛出异常
        }
    }
}