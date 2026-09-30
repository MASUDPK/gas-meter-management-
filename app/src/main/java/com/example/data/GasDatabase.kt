package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY flat ASC")
    fun getAllCustomers(): Flow<List<CustomerFlat>>

    @Query("SELECT * FROM customers WHERE flat = :flat LIMIT 1")
    suspend fun getCustomerByFlat(flat: String): CustomerFlat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerFlat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerFlat>)

    @Query("DELETE FROM customers")
    suspend fun clearAll()
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_history ORDER BY id DESC")
    fun getAllPayments(): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentRecord>)

    @Query("DELETE FROM payment_history")
    suspend fun clearAll()
}

@Database(entities = [CustomerFlat::class, PaymentRecord::class], version = 1, exportSchema = false)
abstract class GasDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        val FIXED_FLATS = listOf(
            "A-2" to "261617108356",
            "A-3" to "261617108354",
            "A-4" to "261617108357",
            "A-5" to "261617108352",
            "A-6" to "261617108351",
            "A-7" to "261617108350",
            "A-8" to "261617108355",

            "B-2" to "261617108343",
            "B-4" to "261617108346",
            "B-5" to "261617108348",
            "B-6" to "261617108344",
            "B-7" to "261617108345",
            "B-8" to "261617108349",

            "C-2" to "261617108360",
            "C-3" to "261617108358",
            "C-4" to "261617108361",
            "C-5" to "261617108342",
            "C-6" to "261617108359",
            "C-7" to "261617108353",
            "C-8" to "261617108347",

            "D-1" to "261617108340",
            "D-2" to "261617108335",
            "D-3" to "261617108336",
            "D-4" to "261617108338",
            "D-5" to "261617108339",
            "D-6" to "261617108334",
            "D-7" to "261617108341",
            "D-8" to "261617108337"
        )

        fun createInitialCustomers(): List<CustomerFlat> {
            return FIXED_FLATS.map { (flat, meter) ->
                CustomerFlat(
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
                    status = "Active"
                )
            }
        }

        @Volatile
        private var INSTANCE: GasDatabase? = null

        fun getDatabase(context: Context): GasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GasDatabase::class.java,
                    "jamila_bhavan_gas.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val initial = createInitialCustomers()
                            getDatabase(context).customerDao().insertAll(initial)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
