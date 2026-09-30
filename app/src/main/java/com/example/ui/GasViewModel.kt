package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CustomerFlat
import com.example.data.GasDatabase
import com.example.data.GasRepository
import com.example.data.PaymentRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GasRepository(GasDatabase.getDatabase(application))

    val customers: StateFlow<List<CustomerFlat>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentRecord>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")
    val filterStatus = MutableStateFlow("All") // "All", "Due", "Paid"
    val billingMonth = MutableStateFlow(
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    )

    // Admin authentication
    val isAdminLoggedIn = MutableStateFlow(true)
    val loginError = MutableStateFlow<String?>(null)

    // Dialogs / Sheets
    val showEntryDialog = MutableStateFlow(false)
    val isNewEntryMode = MutableStateFlow(true)
    val editingFlat = MutableStateFlow<CustomerFlat?>(null)

    val showPaymentDialog = MutableStateFlow(false)
    val paymentTargetFlat = MutableStateFlow<CustomerFlat?>(null)

    val showReportDialog = MutableStateFlow(false)
    val showBackupDialog = MutableStateFlow(false)
    val showHistoryDialog = MutableStateFlow(false)
    val showAdminLoginDialog = MutableStateFlow(false)

    val snackbarMessage = MutableSharedFlow<String>()

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    val filteredCustomers: StateFlow<List<CustomerFlat>> = combine(
        customers,
        searchQuery,
        filterStatus
    ) { list, query, filter ->
        list.filter { c ->
            val matchesQuery = query.isBlank() ||
                    c.flat.contains(query, ignoreCase = true) ||
                    c.name.contains(query, ignoreCase = true) ||
                    c.meter.contains(query, ignoreCase = true) ||
                    c.mobile.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Due" -> c.due > 0.0
                "Paid" -> c.due <= 0.0 && c.bill > 0.0
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats = customers.map { list ->
        val totalFlats = list.size
        val totalUnits = list.sumOf { it.unit }
        val totalBill = list.sumOf { it.bill }
        val totalPaid = list.sumOf { it.paid }
        val totalDue = list.sumOf { it.due }
        val paidCount = list.count { it.due <= 0.0 && it.bill > 0.0 }
        val dueCount = list.count { it.due > 0.0 }
        DashboardStats(
            totalFlats = totalFlats,
            totalUnits = totalUnits,
            totalBill = totalBill,
            totalPaid = totalPaid,
            totalDue = totalDue,
            paidCustomers = paidCount,
            dueCustomers = dueCount
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardStats(28, 0.0, 0.0, 0.0, 0.0, 0, 0)
    )

    fun openNewEntry(flatName: String? = null) {
        val target = if (flatName != null) {
            customers.value.find { it.flat == flatName }
        } else {
            customers.value.firstOrNull()
        }
        isNewEntryMode.value = true
        editingFlat.value = target
        showEntryDialog.value = true
    }

    fun openUpdate(flatName: String? = null) {
        val target = if (flatName != null) {
            customers.value.find { it.flat == flatName }
        } else {
            customers.value.firstOrNull()
        }
        isNewEntryMode.value = false
        editingFlat.value = target
        showEntryDialog.value = true
    }

    fun openPayment(flatName: String? = null) {
        val target = if (flatName != null) {
            customers.value.find { it.flat == flatName }
        } else {
            customers.value.firstOrNull()
        }
        paymentTargetFlat.value = target
        showPaymentDialog.value = true
    }

    fun saveCustomerEntry(customer: CustomerFlat) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            showEntryDialog.value = false
            editingFlat.value = null
            snackbarMessage.emit("Entry saved for Flat ${customer.flat}")
        }
    }

    fun recordPayment(flat: String, amount: Double, date: String, method: String, remarks: String) {
        viewModelScope.launch {
            val success = repository.recordPayment(flat, amount, date, method, remarks)
            if (success) {
                showPaymentDialog.value = false
                paymentTargetFlat.value = null
                snackbarMessage.emit("Payment of ${"%.2f".format(amount)} Tk recorded for Flat $flat")
            } else {
                snackbarMessage.emit("Failed to record payment for Flat $flat")
            }
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            // Data safety protection: do not wipe existing data
            snackbarMessage.emit("Data reset is disabled to protect existing records and payment history.")
        }
    }

    suspend fun getExportJson(): String {
        return repository.exportJson()
    }

    fun importJson(json: String) {
        viewModelScope.launch {
            val success = repository.importJson(json)
            if (success) {
                snackbarMessage.emit("Backup restored successfully")
                showBackupDialog.value = false
            } else {
                snackbarMessage.emit("Invalid backup data format")
            }
        }
    }

    fun loginAdmin(email: String, pass: String): Boolean {
        if (email.isNotBlank() && pass.isNotBlank()) {
            isAdminLoggedIn.value = true
            showAdminLoginDialog.value = false
            loginError.value = null
            return true
        } else {
            loginError.value = "Please enter email and password"
            return false
        }
    }

    fun logoutAdmin() {
        isAdminLoggedIn.value = false
    }
}

data class DashboardStats(
    val totalFlats: Int,
    val totalUnits: Double,
    val totalBill: Double,
    val totalPaid: Double,
    val totalDue: Double,
    val paidCustomers: Int,
    val dueCustomers: Int
)
