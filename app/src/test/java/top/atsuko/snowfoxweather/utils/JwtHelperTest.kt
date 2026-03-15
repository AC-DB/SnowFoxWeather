package top.atsuko.snowfoxweather.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.Security
import java.security.Signature
import java.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JwtHelperTest {

    companion object {
        // 确保测试环境也加载 BouncyCastle
        @JvmStatic
        @BeforeClass
        fun setUp() {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }
    }

    @Test
    fun `generateToken returns valid signed JWT`() {
        // 1. 准备：生成一对临时的 EdDSA 测试密钥
        val kpg = KeyPairGenerator.getInstance("Ed25519", "BC")
        val keyPair = kpg.generateKeyPair()
        val privateKey = keyPair.private
        val publicKey = keyPair.public

        // 将私钥转换为 PEM 格式字符串 (模拟真实输入)
        val privateKeyBytes = privateKey.encoded
        val privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes)
        val mockPrivateKeyContent = "-----BEGIN PRIVATE KEY-----\n$privateKeyBase64\n-----END PRIVATE KEY-----"

        val mockKeyId = "test-key-id"
        val mockProjectId = "test-project-id"

        // 2. 执行：调用 JwtHelper (传入测试密钥)
        val token = JwtHelper.generateToken(
            privateKeyContent = mockPrivateKeyContent,
            keyId = mockKeyId,
            projectId = mockProjectId
        )

        // 3. 验证：Token 结构
        assertNotNull("Token should not be null", token)
        assertTrue("Token should not be empty", token.isNotEmpty())

        val parts = token.split(".")
        assertEquals("JWT should have 3 parts", 3, parts.size)

        val headerJson = String(Base64.getUrlDecoder().decode(parts[0]))
        val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
        val signatureBytes = Base64.getUrlDecoder().decode(parts[2])

        // 4. 验证：Header 内容
        val gson = Gson()
        val headerMap: Map<String, Any> = gson.fromJson(headerJson, object : TypeToken<Map<String, Any>>() {}.type)
        assertEquals("EdDSA", headerMap["alg"])
        assertEquals(mockKeyId, headerMap["kid"])

        // 5. 验证：Payload 内容
        val payloadMap: Map<String, Any> = gson.fromJson(payloadJson, object : TypeToken<Map<String, Any>>() {}.type)
        assertEquals(mockProjectId, payloadMap["sub"])

        // 验证时间戳 (宽容度测试)
        val iat = (payloadMap["iat"] as Number).toLong()
        val exp = (payloadMap["exp"] as Number).toLong()
        assertEquals("Exp should be iat + 900", 900, exp - iat)

        // 6. 验证：签名有效性 (最关键的一步)
        // 使用公钥解密/验证签名，确保是由对应的私钥签发的
        val contentToVerify = "${parts[0]}.${parts[1]}"
        val verifier = Signature.getInstance("EdDSA", "BC")
        verifier.initVerify(publicKey)
        verifier.update(contentToVerify.toByteArray())

        val isVerified = verifier.verify(signatureBytes)
        assertTrue("Signature verification failed", isVerified)
    }

    @Test
    fun `generateToken handles invalid key gracefully`() {
        // 测试传入垃圾数据时是否不会崩溃
        val token = JwtHelper.generateToken(
            privateKeyContent = "invalid-key-data",
            keyId = "id",
            projectId = "project"
        )
        // 根据目前代码逻辑，发生异常通过 printStackTrace 捕获并返回空字符串
        assertEquals("", token)
    }
}