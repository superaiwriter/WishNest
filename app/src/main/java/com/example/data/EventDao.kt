package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: Int): Flow<Event?>

    @Query("SELECT * FROM events WHERE isFavorite = 1")
    fun getFavoriteEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
