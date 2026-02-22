package com.example.destinationalert.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.destinationalert.data.local.entities.Station
import com.example.destinationalert.data.local.entities.Trip
import com.example.destinationalert.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripSetupViewModel @Inject constructor(
    private val repository: TripRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    // Exposure of available stations
    val stations: StateFlow<List<Station>> = repository.getAllStations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(TripSetupUiState())
    val uiState: StateFlow<TripSetupUiState> = _uiState.asStateFlow()

    init {
        // Load stations from JSON asset if DB is empty
        viewModelScope.launch {
            if (repository.getStationCount() == 0) {
                val stationsList = loadStationsFromJson()
                if (stationsList.isNotEmpty()) {
                    repository.insertStations(stationsList)
                }
            }
        }
    }

    private fun loadStationsFromJson(): List<Station> {
        return try {
            val inputStream = context.assets.open("stations.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, Charsets.UTF_8)
            
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<StationJsonModel>>() {}.type
            val jsonStations: List<StationJsonModel> = gson.fromJson(json, type)
            
            jsonStations.map { 
                Station(
                    stationCode = it.id,
                    name = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Helper model for JSON parsing
    data class StationJsonModel(
        val id: String,
        val name: String,
        val latitude: Double,
        val longitude: Double
        // Ignore other fields for now
    )

    fun selectStation(station: Station) {
        _uiState.value = _uiState.value.copy(selectedStation = station)
    }

    fun setAlertRadius(radiusMeters: Int) {
        _uiState.value = _uiState.value.copy(alertRadius = radiusMeters)
    }

    fun startTrip() {
        val state = _uiState.value
        if (state.selectedStation != null) {
            viewModelScope.launch {
                val trip = Trip(
                    destinationId = state.selectedStation.id,
                    destinationName = state.selectedStation.name,
                    destinationLat = state.selectedStation.latitude,
                    destinationLng = state.selectedStation.longitude,
                    alertRadiusMeters = state.alertRadius,
                    isActive = true,
                    transportMode = "General" // Could make this dynamic later
                )
                repository.createTrip(trip)
                _uiState.value = _uiState.value.copy(isTripStarted = true)
            }
        }
    }
    
    fun resetTripStartedEvent() {
         _uiState.value = _uiState.value.copy(isTripStarted = false)
    }
}

data class TripSetupUiState(
    val selectedStation: Station? = null,
    val alertRadius: Int = 1000, // Default 1km
    val isTripStarted: Boolean = false
)
