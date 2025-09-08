package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.entity.HistoryItem

data class PasteRequest(
    val title: String?,
    val content: String,
    val language: String,
    val password: String?,
    val expiration: String,
    val isPrivate: Boolean,
    val author: HistoryItem
)