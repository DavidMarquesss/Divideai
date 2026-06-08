package com.example.divideai.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageBase64: String = ""
)