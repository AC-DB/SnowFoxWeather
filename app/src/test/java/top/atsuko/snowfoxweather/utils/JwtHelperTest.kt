package top.atsuko.snowfoxweather.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.Security
import java.security.Signature
import java.util.Base64
import kotlin.math.abs

class JwtHelperTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun setUp() {
            // 确保测试环境加载 BouncyCastle Provider，否则无法识别 EdDSA/Ed25519
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }

    @Test
    fun `generateTokenInfo returns correct expiry information`() {
        // 1. 准备密钥
        val kpg = KeyPairGenerator.getInstance("Ed25519", "BC")
        val keyPair = kpg.generateKeyPair()
        val privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.private.encoded)
        val mockPrivateKeyContent = "-----BEGIN PRIVATE KEY-----\n$privateKeyBase64\n-----END PRIVATE KEY-----"

        // 2. 执行测试
        val result = JwtHelper.generateTokenInfo(
            privateKeyContent = mockPrivateKeyContent,
            keyId = "kid",
            projectId = "sub"
        )

        // 3. 验证结果对象
        assertNotNull("TokenResult 不应为空", result)
        assertTrue("Token 字符串不应为空", result!!.token.isNotEmpty())

        // 验证过期时间
        // JwtHelper 逻辑：iat = now - 30s, exp = iat + 900s
        // 所以预期过期时间 = now + 870s
        val currentTime = System.currentTimeMillis() / 1000
        val expectedExpiry = currentTime + 870

        // 允许 5 秒左右的计算/执行误差
        val diff = abs(result.expiresAt - expectedExpiry)
        assertTrue("过期时间应接近当前时间+870秒 (实际差值: $diff)", diff < 5)

        // 4. 验证 Token 内容
        verifyJwtContent(result.token, "kid", "sub", keyPair.public)
    }

    /**
     * 辅助方法：验证 JWT 的 Header, Payload 和 签名
     */
    private fun verifyJwtContent(token: String, expectedKeyId: String, expectedSub: String, publicKey: java.security.PublicKey) {
        val parts = token.split(".")
        val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
        val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))

        // 使用 Kotlinx Serialization 解析 JSON
        val headerMap = Json.decodeFromString<JsonObject>(headerJson)
        val payloadMap = Json.decodeFromString<JsonObject>(payloadJson)

        // 验证 Header
        assertEquals("EdDSA", headerMap["alg"]?.jsonPrimitive?.content)
        assertEquals(expectedKeyId, headerMap["kid"]?.jsonPrimitive?.content)

        // 验证 Payload
        assertEquals(expectedSub, payloadMap["sub"]?.jsonPrimitive?.content)
        val iat = payloadMap["iat"]?.jsonPrimitive?.long ?: 0L
        val exp = payloadMap["exp"]?.jsonPrimitive?.long ?: 0L

        // 验证 exp - iat 是否正好是 900 秒（15分钟）
        assertEquals("有效期应为 900 秒", 900, exp - iat)

        // 验证签名
        val contentToVerify = "${parts[0]}.${parts[1]}"
        val signatureBytes = Base64.getUrlDecoder().decode(parts[2])

        val verifier = Signature.getInstance("EdDSA", "BC")
        verifier.initVerify(publicKey)
        verifier.update(contentToVerify.toByteArray())

        assertTrue("签名验证失败", verifier.verify(signatureBytes))
    }
}
