package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GasRepository(private val database: GasDatabase) {
    private val customerDao = database.customerDao()
    private val paymentDao = database.paymentDao()

    val allCustomers: Flow<List<CustomerFlat>> = customerDao.getAllCustomers()
    val allPayments: Flow<List<PaymentRecord>> = paymentDao.getAllPayments()

    suspend fun ensureDefaultData() = withContext(Dispatchers.IO) {
        val existing = customerDao.getAllCustomers().first()
        if (existing.isEmpty()) {
            customerDao.insertAll(GasDatabase.createInitialCustomers())
        }
    }

    suspend fun saveCustomer(customer: CustomerFlat) = withContext(Dispatchers.IO) {
        customerDao.insertCustomer(customer)
    }

    suspend fun clearCustomer(flat: String) = withContext(Dispatchers.IO) {
        val existing = customerDao.getCustomerByFlat(flat)
        val meter = existing?.meter ?: (GasDatabase.FIXED_FLATS.find { it.first == flat }?.second ?: "")
        val resetCustomer = CustomerFlat(
            flat = flat,
            meter = meter,
            name = "",
            mobile = "",
            previous = 0.0,
            current = 0.0,
            unit = 0.0,
            gasRate = 290.0,
            serviceCharge = 0.0,
            previousDue = 0.0,
            discount = 0.0,
            lateFee = 0.0,
            bill = 0.0,
            paid = 0.0,
            due = 0.0,
            status = "Active",
            readingDate = "",
            remarks = ""
        )
        customerDao.insertCustomer(resetCustomer)
    }

    suspend fun recordPayment(
        flat: String,
        amount: Double,
        date: String,
        method: String,
        remarks: String
    ): Boolean = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerByFlat(flat) ?: return@withContext false
        val newPaid = customer.paid + amount
        val newDue = (customer.bill - newPaid).coerceAtLeast(0.0)
        val newStatus = if (newDue <= 0.0) "Paid" else "Partial"

        val updatedCustomer = customer.copy(
            paid = newPaid,
            due = newDue,
            status = newStatus
        )
        customerDao.insertCustomer(updatedCustomer)

        val paymentRecord = PaymentRecord(
            flat = flat,
            customer = customer.name.ifBlank { "Customer" },
            amount = amount,
            date = date,
            method = method,
            remarks = remarks
        )
        paymentDao.insertPayment(paymentRecord)
        true
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        // Data Safety Guard: Never clear customer data or payment history
        ensureDefaultData()
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val customers = customerDao.getAllCustomers().first()
        val payments = paymentDao.getAllPayments().first()

        val root = JSONObject()
        root.put("building", "Jamila Bhavan")
        root.put("version", "2.0")

        val customerArray = JSONArray()
        customers.forEach { c ->
            val obj = JSONObject()
            obj.put("flat", c.flat)
            obj.put("meter", c.meter)
            obj.put("name", c.name)
            obj.put("mobile", c.mobile)
            obj.put("previous", c.previous)
            obj.put("current", c.current)
            obj.put("unit", c.unit)
            obj.put("gasRate", c.gasRate)
            obj.put("serviceCharge", c.serviceCharge)
            obj.put("previousDue", c.previousDue)
            obj.put("discount", c.discount)
            obj.put("lateFee", c.lateFee)
            obj.put("bill", c.bill)
            obj.put("paid", c.paid)
            obj.put("due", c.due)
            obj.put("status", c.status)
            obj.put("readingDate", c.readingDate)
            obj.put("remarks", c.remarks)
            customerArray.put(obj)
        }
        root.put("customers", customerArray)

        val paymentArray = JSONArray()
        payments.forEach { p ->
            val obj = JSONObject()
            obj.put("flat", p.flat)
            obj.put("customer", p.customer)
            obj.put("amount", p.amount)
            obj.put("date", p.date)
            obj.put("method", p.method)
            obj.put("remarks", p.remarks)
            paymentArray.put(obj)
        }
        root.put("paymentHistory", paymentArray)

        root.toString(2)
    }

    suspend fun importJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("customers")) {
                val customerArray = root.getJSONArray("customers")
                val importedCustomers = mutableListOf<CustomerFlat>()
                for (i in 0 until customerArray.length()) {
                    val obj = customerArray.getJSONObject(i)
                    val flat = obj.optString("flat")
                    if (flat.isNotBlank()) {
                        val meter = obj.optString("meter", GasDatabase.FIXED_FLATS.find { it.first == flat }?.second ?: "")
                        importedCustomers.add(
                            CustomerFlat(
                                flat = flat,
                                meter = meter,
                                name = obj.optString("name", ""),
                                mobile = obj.optString("mobile", ""),
                                previous = obj.optDouble("previous", 0.0),
                                current = obj.optDouble("current", 0.0),
                                unit = obj.optDouble("unit", 0.0),
                                gasRate = obj.optDouble("gasRate", 290.0),
                                serviceCharge = obj.optDouble("serviceCharge", 0.0),
                                previousDue = obj.optDouble("previousDue", 0.0),
                                discount = obj.optDouble("discount", 0.0),
                                lateFee = obj.optDouble("lateFee", 0.0),
                                bill = obj.optDouble("bill", 0.0),
                                paid = obj.optDouble("paid", 0.0),
                                due = obj.optDouble("due", 0.0),
                                status = obj.optString("status", "Active"),
                                readingDate = obj.optString("readingDate", ""),
                                remarks = obj.optString("remarks", "")
                            )
                        )
                    }
                }
                if (importedCustomers.isNotEmpty()) {
                    customerDao.insertAll(importedCustomers)
                }
            }

            if (root.has("paymentHistory")) {
                val paymentArray = root.getJSONArray("paymentHistory")
                val importedPayments = mutableListOf<PaymentRecord>()
                for (i in 0 until paymentArray.length()) {
                    val obj = paymentArray.getJSONObject(i)
                    importedPayments.add(
                        PaymentRecord(
                            flat = obj.optString("flat", ""),
                            customer = obj.optString("customer", ""),
                            amount = obj.optDouble("amount", 0.0),
                            date = obj.optString("date", ""),
                            method = obj.optString("method", "Cash"),
                            remarks = obj.optString("remarks", "")
                        )
                    )
                }
                if (importedPayments.isNotEmpty()) {
                    paymentDao.insertAll(importedPayments)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
