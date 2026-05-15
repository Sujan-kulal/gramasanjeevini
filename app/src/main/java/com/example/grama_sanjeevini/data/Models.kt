package com.example.grama_sanjeevini.data

import androidx.compose.runtime.mutableStateListOf
import java.util.Calendar
import java.util.Date
import java.util.UUID

data class Pharmacy(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val village: String,
    val address: String,
    val contact: String,
    val pin: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val pharmacyId: String,
    var stockLevel: Int,
    val isLifeSaving: Boolean,
    val expiryDate: Date,
    val price: Double,
) {
    fun isNearExpiry(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 1) // Within 1 month
        return expiryDate.before(calendar.time) && expiryDate.after(Date())
    }
    
    fun isExpired(): Boolean = expiryDate.before(Date())
}

// Global empty lists for production app
val pharmacies = mutableStateListOf<Pharmacy>()
val medicines = mutableStateListOf<Medicine>()

fun createDate(monthsFromNow: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, monthsFromNow)
    return calendar.time
}
