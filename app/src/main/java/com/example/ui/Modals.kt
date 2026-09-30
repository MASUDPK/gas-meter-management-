package com.example.ui

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CustomerFlat
import com.example.data.GasDatabase
import com.example.data.PaymentRecord
import com.example.ui.theme.StatusDue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDialog(
    initialCustomer: CustomerFlat?,
    allCustomers: List<CustomerFlat>,
    isNewEntry: Boolean,
    onDismiss: () -> Unit,
    onSave: (CustomerFlat) -> Unit
) {
    val flatsList = remember { GasDatabase.FIXED_FLATS.map { it.first } }
    var selectedFlat by remember { mutableStateOf(initialCustomer?.flat ?: flatsList.first()) }
    
    // Find customer for the currently selected flat
    val currentCustomer = allCustomers.find { it.flat == selectedFlat } ?: initialCustomer

    var meterNo by remember(selectedFlat) {
        mutableStateOf(currentCustomer?.meter ?: (GasDatabase.FIXED_FLATS.find { it.first == selectedFlat }?.second ?: ""))
    }
    var customerName by remember(selectedFlat) { mutableStateOf(currentCustomer?.name ?: "") }
    var mobileNumber by remember(selectedFlat) { mutableStateOf(currentCustomer?.mobile ?: "") }
    
    // In New Entry mode: Previous = previous Current reading, Current = blank
    // In Update mode: Previous = previous reading, Current = current reading
    var previousReadingText by remember(selectedFlat, isNewEntry) {
        val prevVal = if (isNewEntry) {
            if ((currentCustomer?.current ?: 0.0) > 0.0) currentCustomer!!.current
            else currentCustomer?.previous ?: 0.0
        } else {
            currentCustomer?.previous ?: 0.0
        }
        mutableStateOf(if (prevVal == 0.0) "" else prevVal.toString())
    }
    var currentReadingText by remember(selectedFlat, isNewEntry) {
        val currVal = if (isNewEntry) {
            ""
        } else {
            if ((currentCustomer?.current ?: 0.0) == 0.0) "" else currentCustomer?.current.toString()
        }
        mutableStateOf(currVal)
    }
    var gasRateText by remember(selectedFlat) {
        mutableStateOf((currentCustomer?.gasRate ?: 290.0).toString())
    }
    var serviceChargeText by remember(selectedFlat) {
        mutableStateOf((currentCustomer?.serviceCharge ?: 0.0).toString())
    }
    var previousDueText by remember(selectedFlat, isNewEntry) {
        val prevDue = if (isNewEntry) {
            currentCustomer?.due ?: 0.0
        } else {
            currentCustomer?.previousDue ?: 0.0
        }
        mutableStateOf(prevDue.toString())
    }
    var discountText by remember(selectedFlat) {
        mutableStateOf((currentCustomer?.discount ?: 0.0).toString())
    }
    var lateFeeText by remember(selectedFlat) {
        mutableStateOf((currentCustomer?.lateFee ?: 0.0).toString())
    }
    var receivedAmountText by remember(selectedFlat, isNewEntry) {
        val rec = if (isNewEntry) {
            ""
        } else {
            if ((currentCustomer?.paid ?: 0.0) == 0.0) "" else currentCustomer?.paid.toString()
        }
        mutableStateOf(rec)
    }
    var readingDate by remember(selectedFlat) {
        mutableStateOf(
            currentCustomer?.readingDate?.ifBlank {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }
    var remarks by remember(selectedFlat) { mutableStateOf(currentCustomer?.remarks ?: "") }

    var flatDropdownExpanded by remember { mutableStateOf(false) }

    // Computations
    val previous = previousReadingText.toDoubleOrNull() ?: 0.0
    val current = currentReadingText.toDoubleOrNull() ?: 0.0
    val rate = gasRateText.toDoubleOrNull() ?: 290.0
    val service = serviceChargeText.toDoubleOrNull() ?: 0.0
    val prevDue = previousDueText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val lateFee = lateFeeText.toDoubleOrNull() ?: 0.0
    val received = receivedAmountText.toDoubleOrNull() ?: 0.0

    val unit = (current - previous).coerceAtLeast(0.0)
    val totalBill = (unit * rate) + service + prevDue + lateFee - discount
    val currentDue = (totalBill - received).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("entry_dialog_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isNewEntry) "New Month Entry" else "Update Flat Record",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = if (isNewEntry) Color(0xFF047857) else Color(0xFF1D4ED8),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isNewEntry) "NEW" else "UPDATE",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Flat Number Dropdown
                    ExposedDropdownMenuBox(
                        expanded = flatDropdownExpanded,
                        onExpandedChange = { flatDropdownExpanded = !flatDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedFlat,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Flat Number (28 Flats)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flatDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("flat_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = flatDropdownExpanded,
                            onDismissRequest = { flatDropdownExpanded = false }
                        ) {
                            flatsList.forEach { flat ->
                                DropdownMenuItem(
                                    text = { Text(flat) },
                                    onClick = {
                                        selectedFlat = flat
                                        flatDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Meter Number (Readonly fixed)
                    OutlinedTextField(
                        value = meterNo,
                        onValueChange = { meterNo = it },
                        label = { Text("Meter Number (Fixed)") },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )

                    // Customer Name & Mobile
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input")
                    )

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mobile_number_input")
                    )

                    // Meter Readings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = previousReadingText,
                            onValueChange = { previousReadingText = it },
                            label = { Text("Previous") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("previous_reading_input")
                        )
                        OutlinedTextField(
                            value = currentReadingText,
                            onValueChange = { currentReadingText = it },
                            label = { Text("Current") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("current_reading_input")
                        )
                    }

                    // Rates and adjustments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = gasRateText,
                            onValueChange = { gasRateText = it },
                            label = { Text("Gas Rate (Tk)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = serviceChargeText,
                            onValueChange = { serviceChargeText = it },
                            label = { Text("Service Chg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = previousDueText,
                            onValueChange = { previousDueText = it },
                            label = { Text("Prev Due") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it },
                            label = { Text("Discount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lateFeeText,
                            onValueChange = { lateFeeText = it },
                            label = { Text("Late Fee") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Calculation Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Calculation Preview",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Calculated Units:")
                                Text("${"%.2f".format(unit)} Units", fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Bill:")
                                Text("${"%.2f".format(totalBill)} Tk", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Received Amount
                    OutlinedTextField(
                        value = receivedAmountText,
                        onValueChange = { receivedAmountText = it },
                        label = { Text("Received Amount (Paid)") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("received_amount_input")
                    )

                    // Current Due Result Box
                    Surface(
                        color = if (currentDue <= 0.0) StatusPaid.copy(alpha = 0.15f) else StatusDue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Due:", fontWeight = FontWeight.Bold)
                            Text(
                                "${"%.2f".format(currentDue)} Tk (${if (currentDue <= 0.0) "PAID" else "DUE"})",
                                fontWeight = FontWeight.Bold,
                                color = if (currentDue <= 0.0) StatusPaid else StatusDue,
                                fontSize = 16.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = readingDate,
                        onValueChange = { readingDate = it },
                        label = { Text("Reading Date") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                // Action Buttons at bottom
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val status = if (currentDue <= 0.0) "Paid" else if (received > 0.0) "Partial" else "Due"
                                val updated = CustomerFlat(
                                    flat = selectedFlat,
                                    meter = meterNo,
                                    name = customerName,
                                    mobile = mobileNumber,
                                    previous = previous,
                                    current = current,
                                    unit = unit,
                                    gasRate = rate,
                                    serviceCharge = service,
                                    previousDue = prevDue,
                                    discount = discount,
                                    lateFee = lateFee,
                                    bill = totalBill,
                                    paid = received,
                                    due = currentDue,
                                    status = status,
                                    readingDate = readingDate,
                                    remarks = remarks
                                )
                                onSave(updated)
                            },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("save_entry_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isNewEntry) "Save New Entry" else "Update Record")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    initialCustomer: CustomerFlat?,
    allCustomers: List<CustomerFlat>,
    onDismiss: () -> Unit,
    onSavePayment: (flat: String, amount: Double, date: String, method: String, remarks: String) -> Unit
) {
    val flatsList = remember { GasDatabase.FIXED_FLATS.map { it.first } }
    var selectedFlat by remember { mutableStateOf(initialCustomer?.flat ?: flatsList.first()) }
    val currentCustomer = allCustomers.find { it.flat == selectedFlat } ?: initialCustomer

    var customerName by remember(selectedFlat) { mutableStateOf(currentCustomer?.name ?: "") }
    var totalDue by remember(selectedFlat) { mutableStateOf(currentCustomer?.due ?: 0.0) }

    var paymentAmountText by remember { mutableStateOf(if (totalDue > 0.0) totalDue.toString() else "") }
    var paymentDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var remarks by remember { mutableStateOf("") }

    var flatDropdownExpanded by remember { mutableStateOf(false) }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    val methods = listOf("Cash", "Bank", "Mobile Banking")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_dialog_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Record Bill Payment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Flat dropdown
                ExposedDropdownMenuBox(
                    expanded = flatDropdownExpanded,
                    onExpandedChange = { flatDropdownExpanded = !flatDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFlat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Flat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = flatDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = flatDropdownExpanded,
                        onDismissRequest = { flatDropdownExpanded = false }
                    ) {
                        flatsList.forEach { flat ->
                            DropdownMenuItem(
                                text = { Text(flat) },
                                onClick = {
                                    selectedFlat = flat
                                    val match = allCustomers.find { it.flat == flat }
                                    customerName = match?.name ?: ""
                                    totalDue = match?.due ?: 0.0
                                    paymentAmountText = if (totalDue > 0.0) totalDue.toString() else ""
                                    flatDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Customer info & current due
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Customer", style = MaterialTheme.typography.labelSmall)
                        Text(
                            customerName.ifBlank { "Not Assigned" },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Outstanding Due", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${"%.2f".format(totalDue)} Tk",
                            fontWeight = FontWeight.Bold,
                            color = if (totalDue > 0.0) StatusDue else StatusPaid
                        )
                    }
                }

                // Payment amount input
                OutlinedTextField(
                    value = paymentAmountText,
                    onValueChange = { paymentAmountText = it },
                    label = { Text("Payment Amount (Tk)") },
                    leadingIcon = { Icon(Icons.Default.Wallet, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                // Date
                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Payment method
                ExposedDropdownMenuBox(
                    expanded = methodDropdownExpanded,
                    onExpandedChange = { methodDropdownExpanded = !methodDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = methodDropdownExpanded,
                        onDismissRequest = { methodDropdownExpanded = false }
                    ) {
                        methods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    paymentMethod = m
                                    methodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / TxID") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0.0) {
                                onSavePayment(selectedFlat, amount, paymentDate, paymentMethod, remarks)
                            }
                        },
                        enabled = (paymentAmountText.toDoubleOrNull() ?: 0.0) > 0.0,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_payment_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save Payment")
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDialog(
    billingMonth: String,
    stats: DashboardStats,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val reportText = remember(billingMonth, stats) {
        """
================================
        JAMILA BHAVAN
 Gas Meter Management System
================================

Billing Month : $billingMonth

Total Flat :
${stats.totalFlats}

Total Unit :
${"%.2f".format(stats.totalUnits)}

Total Bill :
${"%.2f".format(stats.totalBill)} Tk

Total Paid :
${"%.2f".format(stats.totalPaid)} Tk

Total Due :
${"%.2f".format(stats.totalDue)} Tk

-------------------------------

Paid Customer :
${stats.paidCustomers}

Due Customer :
${stats.dueCustomers}

================================

Generated By:
Rezaul Haque
Jamila Bhavan Management
        """.trimIndent()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("report_dialog_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Monthly Gas Meter Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Summary Table
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReportRow("Billing Month", billingMonth, isBold = true)
                            Divider()
                            ReportRow("Total Flats", stats.totalFlats.toString())
                            ReportRow("Total Units", "%.2f".format(stats.totalUnits))
                            ReportRow("Total Bill", "${"%.2f".format(stats.totalBill)} Tk", isBold = true)
                            ReportRow("Total Paid", "${"%.2f".format(stats.totalPaid)} Tk", color = StatusPaid)
                            ReportRow("Total Due", "${"%.2f".format(stats.totalDue)} Tk", color = StatusDue, isBold = true)
                            Divider()
                            ReportRow("Paid Customers", stats.paidCustomers.toString())
                            ReportRow("Due Customers", stats.dueCustomers.toString())
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Receipt Preview", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reportText,
                            modifier = Modifier.padding(12.dp),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Surface(tonalElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Monthly Report"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Share")
                        }

                        Button(
                            onClick = {
                                printReportHtml(context, billingMonth, stats)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Print PDF")
                        }
                    }
                }
            }
        }
    }
}

private fun printReportHtml(context: Context, month: String, stats: DashboardStats) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
    val html = """
        <html>
        <head>
        <title>Jamila Bhavan Gas Report</title>
        <style>
            body { font-family: Arial, sans-serif; padding: 20px; color: #111; }
            h1 { color: #0f766e; text-align: center; margin-bottom: 2px; }
            h3 { text-align: center; color: #555; margin-top: 0; }
            table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            th, td { border: 1px solid #ccc; padding: 10px; text-align: left; }
            th { background-color: #f0fdf4; }
            .total-due { color: #dc2626; font-weight: bold; }
            .total-paid { color: #16a34a; font-weight: bold; }
            .footer { margin-top: 40px; font-size: 12px; color: #777; text-align: center; }
        </style>
        </head>
        <body>
            <h1>JAMILA BHAVAN-1</h1>
            <h3>Gas Meter Management System</h3>
            <p><strong>Billing Month:</strong> $month</p>
            <p><strong>Address:</strong> Model City, Mouchak, Kaliakair, Gazipur</p>
            <table>
                <tr><th>Description</th><th>Value</th></tr>
                <tr><td>Total Flats</td><td>${stats.totalFlats}</td></tr>
                <tr><td>Total Units</td><td>${"%.2f".format(stats.totalUnits)}</td></tr>
                <tr><td>Total Bill</td><td>${"%.2f".format(stats.totalBill)} Tk</td></tr>
                <tr><td>Total Paid</td><td class="total-paid">${"%.2f".format(stats.totalPaid)} Tk</td></tr>
                <tr><td>Total Due</td><td class="total-due">${"%.2f".format(stats.totalDue)} Tk</td></tr>
                <tr><td>Paid Customers</td><td>${stats.paidCustomers}</td></tr>
                <tr><td>Due Customers</td><td>${stats.dueCustomers}</td></tr>
            </table>
            <div class="footer">
                <p>Generated by: Rezaul Haque • Jamila Bhavan Management System</p>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printAdapter = webView.createPrintDocumentAdapter("Jamila_Bhavan_Report_$month")
            printManager.print(
                "Jamila_Bhavan_Report_$month",
                printAdapter,
                PrintAttributes.Builder().build()
            )
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
}

@Composable
private fun ReportRow(label: String, value: String, isBold: Boolean = false, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
fun BackupRestoreDialog(
    onDismiss: () -> Unit,
    onGetJson: suspend () -> String,
    onImportJson: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var jsonTextToImport by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backup_restore_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Backup & Restore Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text("Export your current customer readings and payment history to JSON format, or restore from a previous backup.", style = MaterialTheme.typography.bodySmall)

                // Export buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val json = onGetJson()
                                clipboard.setText(AnnotatedString(json))
                                Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy JSON")
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val json = onGetJson()
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    type = "application/json"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Save or Send Backup"))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share Backup")
                    }
                }

                Divider()

                Text("Import Backup", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = jsonTextToImport,
                    onValueChange = { jsonTextToImport = it },
                    label = { Text("Paste JSON Backup Data") },
                    placeholder = { Text("{\"customers\": [...], \"paymentHistory\": [...]}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        if (jsonTextToImport.isNotBlank()) {
                            onImportJson(jsonTextToImport)
                        }
                    },
                    enabled = jsonTextToImport.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Restore from JSON")
                }

                Divider()

                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Data Safety Active",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                "Database reset is permanently disabled to ensure tenant records, 28 flats, readings, and payment histories are never wiped.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryDialog(
    payments: List<PaymentRecord>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment History (${payments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                if (payments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No payment transactions recorded yet.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payments) { p ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            "Flat ${p.flat} - ${p.customer}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${p.date} • ${p.method}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        if (p.remarks.isNotBlank()) {
                                            Text(
                                                p.remarks,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        "+${"%.2f".format(p.amount)} Tk",
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPaid,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLoginDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("admin@jamilabhavan.com") }
    var password by remember { mutableStateOf("admin123") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Admin Authentication",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    "Sign in as Admin to manage gas readings, record payments, and edit customer profiles.",
                    style = MaterialTheme.typography.bodySmall
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Admin Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onLogin(email, password) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}
