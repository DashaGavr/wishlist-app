package domain

data class Wish(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUri: String?,
    val link: String?,
    val rank: Double,
    val listId: Long
)
