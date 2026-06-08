package com.example.divideai.data.model

data class Group(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconRes: Int? = null,
    val memberIds: List<String> = emptyList(),
    val imageBase64: String = ""
)
