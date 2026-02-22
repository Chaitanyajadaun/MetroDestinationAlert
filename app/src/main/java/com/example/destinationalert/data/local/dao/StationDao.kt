package com.example.destinationalert.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.destinationalert.data.local.entities.Station
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {
    @Query("SELECT * FROM stations ORDER BY name ASC")
    fun getAllStations(): Flow<List<Station>>

    @Query("SELECT * FROM stations WHERE id = :id")
    suspend fun getStationById(id: Long): Station?

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStations(stations: List<Station>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: Station)
}
