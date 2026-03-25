package com.example.divideai.data.model

data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderEmail: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverEmail: String = "",
    val status: String = "pending" // "pending", "accepted", "rejected"
)