package com.example.destinationalert.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip")
data class Trip(
    @PrimaryKey val id: Int = 1, // Single active trip, always ID 1
    val destinationId: Long,
    val destinationName: String, // Denormalized for easier access
    val destinationLat: Double,
    val destinationLng: Double,
    val alertRadiusMeters: Int,
    val isActive: Boolean,
    val transportMode: String, // e.g., "Bus", "Train"
    val createdAt: Long = System.currentTimeMillis()
)
