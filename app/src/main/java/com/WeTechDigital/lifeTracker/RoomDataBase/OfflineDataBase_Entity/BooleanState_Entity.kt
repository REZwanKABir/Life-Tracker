package com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "booleanState",
    indices = [Index(
        value = [
            "state"
        ], unique = true
    )]
        )
data class BooleanState_Entity(
    @PrimaryKey(autoGenerate = false)
    val id:Int,

    @ColumnInfo(name = "state")
    var state:String = "false"



)