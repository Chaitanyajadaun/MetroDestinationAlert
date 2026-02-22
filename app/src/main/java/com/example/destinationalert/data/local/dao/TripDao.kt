package com.example.destinationalert.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.destinationalert.data.local.entities.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip WHERE id = 1 LIMIT 1")
    fun getActiveTripFlow(): Flow<Trip?>

    @Query("SELECT * FROM trip WHERE id = 1 LIMIT 1")
    suspend fun getActiveTrip(): Trip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    @Query("UPDATE trip SET isActive = :isActive WHERE id = 1")
    suspend fun updateTripStatus(isActive: Boolean)
    
    @Query("UPDATE trip SET destinationLat = :lat, destinationLng = :lng WHERE id = 1")
    suspend fun updateDestination(lat: Double, lng: Double)

    @Query("DELETE FROM trip")
    suspend fun clearTrip()
}
