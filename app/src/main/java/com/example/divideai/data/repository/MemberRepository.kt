package com.example.divideai.data.repository

import com.example.divideai.data.model.Member
import com.example.divideai.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class MemberRepository {
    private val db = Firebase.firestore
    private val membersCollection = db.collection("members")


    fun getMembersByGroup(groupId: String, onResult: (List<Member>) -> Unit) {
        membersCollection.whereEqualTo("groupId", groupId).get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Member::class.java)?.copy(id = doc.id)
                }
                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }


    fun addMembersToGroup(groupId: String, usersToAdd: List<User>, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()
        val groupRef = db.collection("groups").document(groupId)
        val userIdsToAdd = mutableListOf<String>()

        usersToAdd.forEach { user ->
            val newDocRef = membersCollection.document()
            val member = Member(
                id = newDocRef.id,
                groupId = groupId,
                userId = user.id,
                name = user.name,
                email = user.email,
                role = "admin"
            )
            batch.set(newDocRef, member)
            userIdsToAdd.add(user.id)
        }

        // Atualiza a propriedade 'memberIds' no documento do grupo
        if (userIdsToAdd.isNotEmpty()) {
            batch.update(groupRef, "memberIds", FieldValue.arrayUnion(*userIdsToAdd.toTypedArray()))
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
    fun deleteMembers(memberIds: List<String>, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()

        memberIds.forEach { id ->
            batch.delete(membersCollection.document(id))
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}