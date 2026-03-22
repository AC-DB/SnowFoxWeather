package top.atsuko.snowfoxweather.data.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject

class LocalDataStoreSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

}