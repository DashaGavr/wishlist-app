package data

import db.WishlistDao
import domain.Wishlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WishlistRepository(private val dao: WishlistDao) {

    fun getAll(): Flow<List<Wishlist>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): Wishlist? =
        dao.getById(id)?.toDomain()

    suspend fun insert(wishlist: Wishlist): Long =
        dao.insert(wishlist.toEntity())

    suspend fun update(wishlist: Wishlist) =
        dao.update(wishlist.toEntity())

    suspend fun delete(wishlist: Wishlist) =
        dao.delete(wishlist.toEntity())
}
