package db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WishDao {

    @Query("SELECT * FROM wishes WHERE listId = :listId ORDER BY rank ASC")
    fun getByList(listId: Long): Flow<List<WishEntity>>

    @Query("SELECT * FROM wishes WHERE id = :id")
    suspend fun getById(id: Long): WishEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(wish: WishEntity): Long

    @Update
    suspend fun update(wish: WishEntity)

    @Delete
    suspend fun delete(wish: WishEntity)

    @Query("DELETE FROM wishes WHERE listId = :listId")
    suspend fun deleteAllByList(listId: Long)
}
