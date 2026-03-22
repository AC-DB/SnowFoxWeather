package top.atsuko.snowfoxweather.data.impl

import top.atsuko.snowfoxweather.data.MainRepository
import javax.inject.Inject

class AllRepository @Inject constructor(
    private val local: LocalDataStoreSource,
    private val remote: RemoteDataSource,
) : MainRepository {
}