package top.atsuko.snowfoxweather.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import top.atsuko.snowfoxweather.data.remote.model.GeoLookupResponse

/**
 * 城市搜索 API
 * 文档参考: https://dev.qweather.com/docs/api/geo/city-lookup/
 */
interface GeoApi {

    /**
     * 搜索城市
     *
     * @param location (必选) 需要查询的城市名称，支持文字、以逗号分隔的经纬度坐标。
     *                 例如：location=beijing 或 location=116.4,39.9
     * @param adm      (可选) 城市的上级行政区划，用于区分重名城市。
     *                 例如：adm=beijing
     * @param range    (可选) 搜索范围，可设定只在某个国家范围内进行搜索。
     *                 例如：range=cn
     * @param number   (可选) 返回结果的数量，取值 range 1-20，默认 10。
     * @param lang     (可选) 多语言设置，默认 zh。
     * @param key      (可选) 用户认证 Key。建议在 NetworkModule 的 Interceptor 中统一处理，此处可传 null。
     */
    @GET("geo/v2/city/lookup")
    suspend fun lookupCity(
        @Query("location") location: String,
        @Query("adm") adm: String? = null,
        @Query("range") range: String? = null,
        @Query("number") number: Int? = 10,
        @Query("lang") lang: String? = "zh",
        @Query("key") key: String? = null
    ): GeoLookupResponse
}