import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.jibanez.miloca.dao.LocationDao
import org.jibanez.miloca.repository.LocationDatabase
import org.jibanez.miloca.repository.LocationRepository
import org.jibanez.miloca.service.location.DefaultLocationClient
import org.jibanez.miloca.service.location.LocationClient
import org.jibanez.miloca.viewmodel.LocationViewModel
import org.jibanez.miloca.viewmodel.MapViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidApplication(),
            LocationDatabase::class.java,
            "location_database"
        ).build()
    }

    // DAO
    fun provideLocationDao(db: LocationDatabase): LocationDao {
        return db.locationDao()
    }
    single { provideLocationDao(get()) }

    // Repository
    single { LocationRepository(get()) }

    // ViewModel
    viewModelOf(::MapViewModel)

    //Location ViewModel
    single<FusedLocationProviderClient> {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }
    single<LocationClient> {
        DefaultLocationClient(androidApplication(), get())
    }

    viewModelOf(::LocationViewModel)
}