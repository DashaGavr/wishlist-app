package data

import db.WishEntity
import domain.Wish

fun WishEntity.toDomain() = Wish(
    id = id,
    title = title,
    description = description,
    imageUri = imageUri,
    link = link,
    rank = rank,
    listId = listId
)

fun Wish.toEntity() = WishEntity(
    id = id,
    title = title,
    description = description,
    imageUri = imageUri,
    link = link,
    rank = rank,
    listId = listId
)
