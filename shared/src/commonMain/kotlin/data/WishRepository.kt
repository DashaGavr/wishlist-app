package data

import db.WishDao
import domain.Wish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WishRepository(private val dao: WishDao) {

    fun getByList(listId: Long): Flow<List<Wish>> =
        dao.getByList(listId).map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): Wish? =
        dao.getById(id)?.toDomain()

    suspend fun insert(wish: Wish): Long =
        dao.insert(wish.toEntity())

    suspend fun update(wish: Wish) =
        dao.update(wish.toEntity())

    suspend fun delete(wish: Wish) =
        dao.delete(wish.toEntity())

    suspend fun deleteAllByList(listId: Long) =
        dao.deleteAllByList(listId)
}
