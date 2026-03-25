package com.example.divideai.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.divideai.data.model.User
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<AuthResult> =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(task.result!!))
                    } else {
                        continuation.resumeWithException(task.exception!!)
                    }
                }
        }

    suspend fun signUp(name: String, email: String, password: String): Result<AuthResult> =
        suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user

                        if (firebaseUser != null) {
                            val newUser = User(
                                id = firebaseUser.uid, name = name, email = email
                            )

                            db.collection("users").document(firebaseUser.uid).set(newUser)
                                .addOnSuccessListener {
                                    continuation.resume(Result.success(task.result!!))
                                }.addOnFailureListener { e ->
                                    continuation.resumeWithException(Exception("Conta criada, mas falha ao salvar perfil: ${e.message}"))
                                }
                        } else {
                            continuation.resumeWithException(task.exception ?: Exception("Erro desconhecido ao obter usuário"))
                        }
                    } else {
                        continuation.resumeWithException(task.exception ?: Exception("Falha na criação de conta"))
                    }
                }
        }

    fun logout() {
        auth.signOut()
    }
}
