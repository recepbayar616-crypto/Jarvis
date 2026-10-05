package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.JarvisDao
import com.example.data.local.entity.AlertThresholdEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.ConversationMessageEntity
import com.example.data.local.entity.DailyAnalyticsEntity
import com.example.data.local.entity.TaskMemoryEntity
import com.example.data.local.entity.VideoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChannelEntity::class,
        VideoEntity::class,
        DailyAnalyticsEntity::class,
        TaskMemoryEntity::class,
        ConversationMessageEntity::class,
        AlertThresholdEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jarvis_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Initialize clean alert thresholds and welcome message (no demo mock data)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).jarvisDao()
                                dao.insertAlerts(
                                    listOf(
                                        AlertThresholdEntity(type = "VIEWS_1K", thresholdValue = 1000, isEnabled = true),
                                        AlertThresholdEntity(type = "VIEWS_10K", thresholdValue = 10000, isEnabled = true),
                                        AlertThresholdEntity(type = "VELOCITY_SPIKE", thresholdValue = 50, isEnabled = true),
                                        AlertThresholdEntity(type = "DROP", thresholdValue = 30, isEnabled = false)
                                    )
                                )
                                dao.insertMessage(
                                    ConversationMessageEntity(
                                        sender = "JARVIS",
                                        message = "JARVIS sistemleri aktif. Google Cloud YouTube Data API v3 anahtarınız sisteme tanımlandı. Ayarlar sekmesinden kanalınızı bağlayıp canlı verilere erişebilirsiniz."
                                    )
                                )
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
