package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AutomationRoutineDao
import com.example.data.local.dao.CommandHistoryDao
import com.example.data.local.dao.ContactAliasDao
import com.example.data.local.dao.CustomShortcutDao
import com.example.data.local.entities.AutomationRoutineEntity
import com.example.data.local.entities.CommandHistoryEntity
import com.example.data.local.entities.ContactAliasEntity
import com.example.data.local.entities.CustomShortcutEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CommandHistoryEntity::class,
        ContactAliasEntity::class,
        CustomShortcutEntity::class,
        AutomationRoutineEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {

    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun contactAliasDao(): ContactAliasDao
    abstract fun customShortcutDao(): CustomShortcutDao
    abstract fun automationRoutineDao(): AutomationRoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AuraDatabase? = null

        fun getDatabase(context: Context): AuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "max_assistant_db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dbInstance = getDatabase(context)
                            // Initial aliases
                            dbInstance.contactAliasDao().insertAlias(
                                ContactAliasEntity(aliasName = "Papa", actualContactName = "Father", phoneNumber = "+919876543210")
                            )
                            dbInstance.contactAliasDao().insertAlias(
                                ContactAliasEntity(aliasName = "Mummy", actualContactName = "Mother", phoneNumber = "+919876543211")
                            )
                            dbInstance.contactAliasDao().insertAlias(
                                ContactAliasEntity(aliasName = "Rahul", actualContactName = "Rahul Sharma", phoneNumber = "+919876543212")
                            )
                            dbInstance.contactAliasDao().insertAlias(
                                ContactAliasEntity(aliasName = "Amit", actualContactName = "Amit Verma", phoneNumber = "+919876543213")
                            )

                            // Initial shortcuts
                            dbInstance.customShortcutDao().insertShortcut(
                                CustomShortcutEntity(
                                    triggerPhrase = "Movie time",
                                    actionDescription = "Silent mode on and open YouTube",
                                    targetCommand = "Silent mode on karo aur YouTube kholo"
                                )
                            )
                            dbInstance.customShortcutDao().insertShortcut(
                                CustomShortcutEntity(
                                    triggerPhrase = "Driving mode",
                                    actionDescription = "Bluetooth settings and Maps",
                                    targetCommand = "Bluetooth settings kholo aur Maps kholo"
                                )
                            )

                            // Initial automation routines
                            dbInstance.automationRoutineDao().insertRoutine(
                                AutomationRoutineEntity(
                                    title = "Morning Routine",
                                    triggerTime = "07:00 AM",
                                    description = "7:00 AM Alarm → Weather Check → Unread Notifications",
                                    commandsSequence = "7 baje ka alarm laga do\nNotifications padho",
                                    isEnabled = true,
                                    iconCategory = "MORNING"
                                )
                            )
                            dbInstance.automationRoutineDao().insertRoutine(
                                AutomationRoutineEntity(
                                    title = "Focus Work Routine",
                                    triggerTime = "10:00 AM",
                                    description = "Volume 20% → Silent Mode → Open Chrome",
                                    commandsSequence = "Volume 20 percent karo\nSilent mode on karo\nChrome kholo",
                                    isEnabled = true,
                                    iconCategory = "WORK"
                                )
                            )
                            dbInstance.automationRoutineDao().insertRoutine(
                                AutomationRoutineEntity(
                                    title = "Night Sleep Routine",
                                    triggerTime = "11:00 PM",
                                    description = "Silent Mode → 6:30 AM Alarm → Lock Screen",
                                    commandsSequence = "Silent mode on karo\n6 baje ka alarm laga do",
                                    isEnabled = true,
                                    iconCategory = "NIGHT"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
