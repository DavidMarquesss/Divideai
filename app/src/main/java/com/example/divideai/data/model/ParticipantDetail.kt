package com.example.divideai.data.model

data class ParticipantDetail(
    val userId: String,
    val name: String,
    val email: String,
    val amountOwed: Double,
    val paid: Boolean
)