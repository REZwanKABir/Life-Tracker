package com.WeTechDigital.lifeTracker.RoomDataBase.api

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.WeTechDigital.lifeTracker.RoomDataBase.OfflineDataBase_Entity.*
import com.WeTechDigital.lifeTracker.utils.Converters


@Database(
    entities = [TrustedContacts_Entity::class,
        PersonalInformation_Entity::class,
        SIMCard_Entity::class,
        BooleanState_Entity::class,
        DeleteCache_Entity::class], version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OfflineDataBase : RoomDatabase() {

    abstract fun userContactsDao(): OfflineDataBaseDao

    companion object {

        @Volatile
        private var INSTANCE: OfflineDataBase? = null

        fun getDatabase(context: Context): OfflineDataBase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OfflineDataBase::class.java,
                    "user_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }

    }
}