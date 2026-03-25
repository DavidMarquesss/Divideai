package com.example.divideai.data.repository

import com.example.divideai.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserRepository {
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    fun getAllUsers(onResult: (List<User>) -> Unit) {
        usersCollection.get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }
                onResult(users)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getUserById(userId: String, onResult: (User?) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = doc.toObject(User::class.java)
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}