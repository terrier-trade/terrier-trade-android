package edu.bu.terriertrade.data.repository

import kotlinx.coroutines.flow.Flow

/** Hearted listings. Stored on the device. */
interface SavedListingsRepository {
    fun observeSavedIds(): Flow<Set<String>>

    suspend fun toggleSaved(listingId: String)
}
