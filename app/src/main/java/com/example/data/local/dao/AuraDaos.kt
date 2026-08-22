package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AutomationRoutineEntity
import com.example.data.local.entities.CommandHistoryEntity
import com.example.data.local.entities.ContactAliasEntity
import com.example.data.local.entities.CustomShortcutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandHistoryDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<CommandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CommandHistoryEntity): Long

    @Query("DELETE FROM command_history")
    suspend fun clearAllHistory()
}

@Dao
interface ContactAliasDao {
    @Query("SELECT * FROM contact_aliases ORDER BY aliasName ASC")
    fun getAllAliases(): Flow<List<ContactAliasEntity>>

    @Query("SELECT * FROM contact_aliases WHERE LOWER(aliasName) = LOWER(:alias) LIMIT 1")
    suspend fun findByAlias(alias: String): ContactAliasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: ContactAliasEntity): Long

    @Delete
    suspend fun deleteAlias(alias: ContactAliasEntity)
}

@Dao
interface CustomShortcutDao {
    @Query("SELECT * FROM custom_shortcuts ORDER BY triggerPhrase ASC")
    fun getAllShortcuts(): Flow<List<CustomShortcutEntity>>

    @Query("SELECT * FROM custom_shortcuts WHERE LOWER(triggerPhrase) = LOWER(:phrase) AND isEnabled = 1 LIMIT 1")
    suspend fun findShortcut(phrase: String): CustomShortcutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: CustomShortcutEntity): Long

    @Delete
    suspend fun deleteShortcut(shortcut: CustomShortcutEntity)
}

@Dao
interface AutomationRoutineDao {
    @Query("SELECT * FROM automation_routines ORDER BY id ASC")
    fun getAllRoutines(): Flow<List<AutomationRoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: AutomationRoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: AutomationRoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: AutomationRoutineEntity)
}
