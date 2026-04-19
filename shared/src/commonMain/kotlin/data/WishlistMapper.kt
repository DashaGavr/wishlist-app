package data

import db.WishlistEntity
import domain.Wishlist

fun WishlistEntity.toDomain() = Wishlist(id = id, name = name, emoji = emoji)

fun Wishlist.toEntity() = WishlistEntity(id = id, name = name, emoji = emoji)
