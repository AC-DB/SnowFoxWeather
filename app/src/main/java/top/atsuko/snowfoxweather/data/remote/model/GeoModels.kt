package top.atsuko.snowfoxweather.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 城市搜索响应体
 */
@Serializable
data class GeoLookupResponse(
    @SerialName("code") val code: String? = null,
    @SerialName("location") val locations: List<GeoLocation>? = null,
    @SerialName("refer") val refer: Refer? = null
) {
    fun isSuccess() = code == "200"
}

/**
 * 城市/地区位置信息
 */
@Serializable
data class GeoLocation(
    @SerialName("name") val name: String? = null,
    @SerialName("id") val id: String? = null,
    @SerialName("lat") val lat: String? = null,
    @SerialName("lon") val lon: String? = null,
    @SerialName("adm2") val adm2: String? = null,
    @SerialName("adm1") val adm1: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("tz") val tz: String? = null,
    @SerialName("utcOffset") val utcOffset: String? = null,
    @SerialName("isDst") val isDst: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("rank") val rank: String? = null,
    @SerialName("fxLink") val fxLink: String? = null
)

@Serializable
data class Refer(
    @SerialName("sources") val sources: List<String>? = null,
    @SerialName("license") val license: List<String>? = null
)