package db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {

    @Query("SELECT * FROM wishlists ORDER BY name ASC")
    fun getAll(): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlists WHERE id = :id")
    suspend fun getById(id: Long): WishlistEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(wishlist: WishlistEntity): Long

    @Update
    suspend fun update(wishlist: WishlistEntity)

    @Delete
    suspend fun delete(wishlist: WishlistEntity)
}
