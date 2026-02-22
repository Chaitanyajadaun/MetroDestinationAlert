package com.example.destinationalert.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.destinationalert.data.local.AppDatabase
import com.example.destinationalert.data.local.dao.StationDao
import com.example.destinationalert.data.local.dao.TripDao
import com.example.destinationalert.data.local.entities.Station
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "destination_alert.db"
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate data if needed
                // This is a bit hacky to do here without a Provider, but sufficient for initialization
                // ideally use a Worker or CoroutineScope injected properly
            }
        })
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideStationDao(database: AppDatabase): StationDao {
        return database.stationDao()
    }

    @Provides
    @Singleton
    fun provideTripDao(database: AppDatabase): TripDao {
        return database.tripDao()
    }
}
