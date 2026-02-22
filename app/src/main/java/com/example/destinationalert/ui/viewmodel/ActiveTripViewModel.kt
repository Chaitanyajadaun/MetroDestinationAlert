package com.example.destinationalert.ui.viewmodel

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.destinationalert.MainActivity
import com.example.destinationalert.R
import com.example.destinationalert.data.local.entities.Trip
import com.example.destinationalert.data.repository.TripRepository
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveTripViewModel @Inject constructor(
    private val repository: TripRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val activeTrip: StateFlow<Trip?> = repository.getActiveTrip()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun cancelTrip() {
        viewModelScope.launch {
            repository.clearTrip()
        }
    }

    fun testAlarm() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "destination_alert_channel")
            .setContentTitle("TEST: Arriving at Destination!")
            .setContentText("This is a test notification to verify sounds and visibility.")
            .setSmallIcon(R.drawable.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(999, notification)
    }

    @SuppressLint("MissingPermission")
    fun simulateArrival() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                viewModelScope.launch {
                    repository.updateDestination(it.latitude, it.longitude)
                }
            }
        }
    }
}
