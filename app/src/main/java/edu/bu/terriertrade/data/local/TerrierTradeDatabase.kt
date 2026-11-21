package edu.bu.terriertrade.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app's Room database. It only holds a cache of data that also lives in Firestore,
 * so when the version goes up it is simply wiped and refilled (no migrations to write).
 * Bump [version] whenever an entity changes. Version 2 added the search index (ListingFtsEntity).
 */
@Database(entities = [ListingEntity::class, ListingFtsEntity::class], version = 2, exportSchema = false)
abstract class TerrierTradeDatabase : RoomDatabase() {

    abstract fun listingDao(): ListingDao

    companion object {
        private const val NAME = "terrier_trade.db"

        /** Call once, from AppContainer. */
        fun build(context: Context): TerrierTradeDatabase =
            Room.databaseBuilder(context.applicationContext, TerrierTradeDatabase::class.java, NAME)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}