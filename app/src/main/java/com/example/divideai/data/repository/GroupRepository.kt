package com.example.divideai.data.repository
import com.example.divideai.data.model.Group
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class GroupRepository {
    private val db = Firebase.firestore
    private val groupsCollection = db.collection("groups")

    fun addGroup(group: Group, onComplete: (Boolean, String?, String?) -> Unit) {
        val document = groupsCollection.document()
        val newGroup = group.copy(id = document.id)

        document.set(newGroup)
            .addOnSuccessListener {
                onComplete(true, null, document.id)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message, null)
            }
    }

    fun updateGroup(group: Group, onComplete: (Boolean, String?) -> Unit) {
        groupsCollection.document(group.id).set(group)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun getGroups(userId: String, onResult: (List<Group>) -> Unit) {
        groupsCollection.whereArrayContains("memberIds", userId).get()
            .addOnSuccessListener { result ->
                val groupsList = result.documents.mapNotNull { document ->
                    document.toObject(Group::class.java)
                }
                onResult(groupsList)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getGroupById(groupId: String, onResult: (Group?) -> Unit) {
        groupsCollection.document(groupId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val group = document.toObject(Group::class.java)
                    onResult(group)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun deleteGroup(groupId: String, onComplete: (Boolean, String?) -> Unit) {
        groupsCollection.document(groupId).delete()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun deleteGroups(groupIds: List<String>, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()

        for (id in groupIds) {
            val group = groupsCollection.document(id)
            batch.delete(group)
        }

        batch.commit()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}

