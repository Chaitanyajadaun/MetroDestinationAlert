package com.example.destinationalert.data.repository

import com.example.destinationalert.data.local.dao.StationDao
import com.example.destinationalert.data.local.dao.TripDao
import com.example.destinationalert.data.local.entities.Station
import com.example.destinationalert.data.local.entities.Trip
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val stationDao: StationDao,
    private val tripDao: TripDao
) {

    fun getAllStations(): Flow<List<Station>> = stationDao.getAllStations()

    suspend fun insertStations(stations: List<Station>) = stationDao.insertStations(stations)

    suspend fun getStationCount(): Int = stationDao.getCount()

    fun getActiveTrip(): Flow<Trip?> = tripDao.getActiveTripFlow()

    suspend fun getActiveTripSnapshot(): Trip? = tripDao.getActiveTrip()

    suspend fun createTrip(trip: Trip) {
        tripDao.insertTrip(trip)
    }

    suspend fun completeTrip() {
        tripDao.updateTripStatus(false)
    }
    
    suspend fun clearTrip() {
        tripDao.clearTrip()
    }

    suspend fun updateDestination(lat: Double, lng: Double) {
        tripDao.updateDestination(lat, lng)
    }
}
