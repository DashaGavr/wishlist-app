package db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder(ctx, AppDatabase::class.java, "wishlist.db")
