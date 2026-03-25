package com.example.divideai.data.repository

import com.example.divideai.data.model.Expense
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ExpenseRepository {
    private val db = Firebase.firestore
    private val expensesCollection = db.collection("expenses")

    fun addExpense(expense: Expense, onComplete: (Boolean, String?) -> Unit) {
        val document = expensesCollection.document()
        val newExpense = expense.copy(id = document.id)

        document.set(newExpense)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun updateExpense(expense: Expense, onComplete: (Boolean, String?) -> Unit) {
        expensesCollection.document(expense.id).set(expense)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun getExpensesByGroup(groupId: String, onResult: (List<Expense>) -> Unit) {
        expensesCollection.whereEqualTo("groupId", groupId).get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.toObject(Expense::class.java) }
                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getExpenseById(expenseId: String, onResult: (Expense?) -> Unit) {
        expensesCollection.document(expenseId).get()
            .addOnSuccessListener { onResult(it.toObject(Expense::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun deleteExpenses(expenseIds: List<String>, onComplete: (Boolean) -> Unit) {
        val batch = db.batch()
        for (id in expenseIds) {
            batch.delete(expensesCollection.document(id))
        }
        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getMyExpenses(userId: String, onResult: (List<Expense>) -> Unit) {
        expensesCollection.get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.toObject(Expense::class.java) }
                val myExpenses = list.filter { expense ->
                    expense.participants.any { it.userId == userId }
                }
                onResult(myExpenses)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun getReceivables(userId: String, onResult: (List<Expense>) -> Unit) {
        expensesCollection.whereEqualTo("payerId", userId).get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.toObject(Expense::class.java) }
                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}