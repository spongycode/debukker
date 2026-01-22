package com.spongycode.debukker.debug.network

import com.spongycode.debukker.debug.models.NetworkTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


object NetworkLogger {
    private val _transactions = MutableStateFlow<List<NetworkTransaction>>(emptyList())
    val transactions: StateFlow<List<NetworkTransaction>> = _transactions.asStateFlow()
    
    private val maxLogSize = 100
    fun logTransaction(transaction: NetworkTransaction) {
        _transactions.update { logs ->
            (logs + transaction).takeLast(maxLogSize)
        }
    }

    fun clearLogs() {
        _transactions.value = emptyList()
    }

    fun filterByUrl(pattern: String): List<NetworkTransaction> {
        if (pattern.isBlank()) return transactions.value
        
        return transactions.value.filter { transaction ->
            try {
                Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(transaction.request.url)
            } catch (e: Exception) {
                transaction.request.url.contains(pattern, ignoreCase = true)
            }
        }
    }

    fun getTransactionById(id: String): NetworkTransaction? {
        return transactions.value.find { it.id == id }
    }
}
