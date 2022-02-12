package com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "deleteCache", indices = [Index(value = ["deleteNumber"], unique = true)])
data class DeleteCache_Entity(

    @PrimaryKey(autoGenerate = false)
    val deleteNumber: String

)
