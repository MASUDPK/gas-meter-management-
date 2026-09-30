package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerFlat(
    @PrimaryKey val flat: String,
    val meter: String,
    val name: String = "",
    val mobile: String = "",
    val previous: Double = 0.0,
    val current: Double = 0.0,
    val unit: Double = 0.0,
    val gasRate: Double = 290.0,
    val serviceCharge: Double = 0.0,
    val previousDue: Double = 0.0,
    val discount: Double = 0.0,
    val lateFee: Double = 0.0,
    val bill: Double = 0.0,
    val paid: Double = 0.0,
    val due: Double = 0.0,
    val status: String = "Active", // "Active", "Paid", "Due", "Partial"
    val readingDate: String = "",
    val remarks: String = ""
)

@Entity(tableName = "payment_history")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flat: String,
    val customer: String,
    val amount: Double,
    val date: String,
    val method: String, // "Cash", "Bank", "Mobile Banking"
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
