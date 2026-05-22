package com.example.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()
    val favoriteEvents: Flow<List<Event>> = eventDao.getFavoriteEvents()

    fun getEventById(id: Int): Flow<Event?> {
        return eventDao.getEventById(id)
    }

    suspend fun insert(event: Event): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun update(event: Event) {
        eventDao.updateEvent(event)
    }

    suspend fun delete(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteAll() {
        eventDao.deleteAllEvents()
    }
}
