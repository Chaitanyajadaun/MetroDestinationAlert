package com.example.destinationalert.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "stations", indices = [Index(value = ["stationCode"], unique = true)])
data class Station(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stationCode: String? = null, // Stores original JSON ID
    val name: String,
    val latitude: Double,
    val longitude: Double
)
