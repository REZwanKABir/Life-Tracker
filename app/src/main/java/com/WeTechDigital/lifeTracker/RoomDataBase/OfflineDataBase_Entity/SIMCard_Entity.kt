package com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sim_table")
data class SIMCard_Entity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val SELECTED_SIM_SLOT: String
)
