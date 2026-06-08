package com.example.divideai.ui.expenses.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.divideai.R
import com.example.divideai.data.model.Expense
import com.example.divideai.data.model.ParticipantDetail
import com.example.divideai.data.repository.ExpenseRepository
import com.example.divideai.data.repository.MemberRepository
import java.text.NumberFormat
import java.util.Locale

class ExpenseDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository = ExpenseRepository()
    private val memberRepository = MemberRepository()

    val expenseData = MutableLiveData<Expense?>()
    val payerName = MutableLiveData<String>()
    val participantsList = MutableLiveData<List<ParticipantDetail>>()

    val progressPercent = MutableLiveData<Int>()
    val summaryText = MutableLiveData<String>()

    fun loadExpenseDetails(expenseId: String) {
        expenseRepository.getExpenseById(expenseId) { expense ->
            if (expense != null) {
                expenseData.value = expense
                loadMembersAndCalculate(expense)
            }
        }
    }

    private fun loadMembersAndCalculate(expense: Expense) {
        memberRepository.getMembersByGroup(expense.groupId) { members ->

            val payer = members.find { it.userId == expense.payerId }
            payerName.value = payer?.let { it.name.ifEmpty { it.email } }
                ?: getApplication<Application>().getString(R.string.unknown_user)

            var totalOwed = 0.0
            var totalPaid = 0.0

            val details = expense.participants.mapNotNull { share ->
                val member = members.find { it.userId == share.userId }
                if (member != null) {
                    totalOwed += share.amountOwed
                    if (share.paid) {
                        totalPaid += share.amountOwed
                    }

                    ParticipantDetail(
                        userId = share.userId,
                        name = member.name,
                        email = member.email,
                        amountOwed = share.amountOwed,
                        paid = share.paid
                    )
                } else null
            }

            participantsList.value = details

            // porcentagem da progressbar
            if (totalOwed > 0) {
                val percent = ((totalPaid / totalOwed) * 100).toInt()
                progressPercent.value = percent
            } else {
                progressPercent.value = 100
            }

            val formatador = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val paidStr = formatador.format(totalPaid)
            val owedStr = formatador.format(totalOwed)

            summaryText.value = "$paidStr / $owedStr"
        }
    }
}