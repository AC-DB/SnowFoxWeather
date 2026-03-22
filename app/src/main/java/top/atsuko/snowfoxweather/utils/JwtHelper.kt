package top.atsuko.snowfoxweather.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bouncycastle.jce.provider.BouncyCastleProvider
import top.atsuko.snowfoxweather.BuildConfig
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Security
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Base64

object JwtHelper {

    init {
        // 注册 BouncyCastle 以支持 EdDSA 算法
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            Security.addProvider(BouncyCastleProvider())
        }
    }

    private val json = Json { encodeDefaults = true }

    // 定义 Token 结果数据类
    data class TokenResult(
        val token: String,
        val expiresAt: Long // 过期时间戳 (秒)
    )

    /**
     * 生成并签名 JWT Token
     * 返回包含 Token 和过期信息的对象
     */
    fun generateTokenInfo(
        privateKeyContent: String = BuildConfig.WEATHER_PRIVATE_KEY,
        keyId: String = BuildConfig.WEATHER_KEY_ID,
        projectId: String = BuildConfig.WEATHER_PROJECT_ID
    ): TokenResult? {
        return try {
            if (privateKeyContent.isBlank() || keyId.isBlank() || projectId.isBlank()) {
                return null
            }

            // 1. 解析私钥
            val cleanKey = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s".toRegex(), "")

            if (cleanKey.isEmpty()) return null

            val privateKeyBytes = Base64.getDecoder().decode(cleanKey)
            val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
            val keyFactory = KeyFactory.getInstance("EdDSA", "BC")
            val privateKey: PrivateKey = keyFactory.generatePrivate(keySpec)

            // 2. 构建 Header
            val headerData = JwtHeader(kid = keyId)
            val headerJson = json.encodeToString(headerData)

            // 3. 构建 Payload
            val iat = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() - 30 // 允许时钟偏移30秒
            val exp = iat + 900 // 15分钟有效期

            val payloadData = JwtPayload(sub = projectId, iat = iat, exp = exp)
            val payloadJson = json.encodeToString(payloadData)

            // 4. Base64URL 编码
            val encoder = Base64.getUrlEncoder().withoutPadding()
            val headerEncoded = encoder.encodeToString(headerJson.toByteArray())
            val payloadEncoded = encoder.encodeToString(payloadJson.toByteArray())
            val dataToSign = "$headerEncoded.$payloadEncoded"

            // 5. 签名
            val signer = Signature.getInstance("EdDSA", "BC")
            signer.initSign(privateKey)
            signer.update(dataToSign.toByteArray())
            val signature = signer.sign()
            val signatureEncoded = encoder.encodeToString(signature)

            // 6. 返回结果
            TokenResult(
                token = "$dataToSign.$signatureEncoded",
                expiresAt = exp
            )

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Serializable
    private data class JwtHeader(val alg: String = "EdDSA", val kid: String)

    @Serializable
    private data class JwtPayload(val sub: String, val iat: Long, val exp: Long)
}
