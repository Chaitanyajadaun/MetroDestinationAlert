package com.example.destinationalert.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.destinationalert.data.local.dao.StationDao
import com.example.destinationalert.data.local.dao.TripDao
import com.example.destinationalert.data.local.entities.Station
import com.example.destinationalert.data.local.entities.Trip

@Database(entities = [Station::class, Trip::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
    abstract fun tripDao(): TripDao
}
