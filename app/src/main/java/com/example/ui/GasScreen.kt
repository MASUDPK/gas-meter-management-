package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerFlat
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GasScreen(viewModel: GasViewModel) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val billingMonth by viewModel.billingMonth.collectAsState()
    val isAdmin by viewModel.isAdminLoggedIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    val showEntryDialog by viewModel.showEntryDialog.collectAsState()
    val isNewEntryMode by viewModel.isNewEntryMode.collectAsState()
    val editingFlat by viewModel.editingFlat.collectAsState()

    val showPaymentDialog by viewModel.showPaymentDialog.collectAsState()
    val paymentTargetFlat by viewModel.paymentTargetFlat.collectAsState()

    val showReportDialog by viewModel.showReportDialog.collectAsState()
    val showBackupDialog by viewModel.showBackupDialog.collectAsState()
    val showHistoryDialog by viewModel.showHistoryDialog.collectAsState()
    val showAdminLoginDialog by viewModel.showAdminLoginDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openNewEntry(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Entry") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_new_entry")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Header
            item {
                HeaderSection(
                    billingMonth = billingMonth,
                    isAdmin = isAdmin,
                    onMonthChange = { newMonth -> viewModel.billingMonth.value = newMonth },
                    onAdminClick = {
                        if (isAdmin) {
                            viewModel.logoutAdmin()
                        } else {
                            viewModel.showAdminLoginDialog.value = true
                        }
                    }
                )
            }

            // Dashboard Stats Cards
            item {
                DashboardSection(stats = stats)
            }

            // Quick Actions Menu
            item {
                QuickMenuSection(
                    onNewEntry = { viewModel.openNewEntry(null) },
                    onUpdate = { viewModel.openUpdate(null) },
                    onPayment = { viewModel.openPayment(null) },
                    onReport = { viewModel.showReportDialog.value = true },
                    onHistory = { viewModel.showHistoryDialog.value = true },
                    onBackup = { viewModel.showBackupDialog.value = true }
                )
            }

            // Search and Status Filters
            item {
                SearchAndFilterSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                    filterStatus = filterStatus,
                    onFilterStatusChange = { viewModel.filterStatus.value = it },
                    resultCount = filteredCustomers.size
                )
            }

            // Flats / Customer Cards
            items(filteredCustomers, key = { it.flat }) { customer ->
                CustomerFlatCard(
                    customer = customer,
                    onNewEntry = { viewModel.openNewEntry(customer.flat) },
                    onUpdate = { viewModel.openUpdate(customer.flat) },
                    onPay = { viewModel.openPayment(customer.flat) },
                    onSendWhatsApp = {
                        sendWhatsAppBill(context, customer)
                    }
                )
            }
        }
    }

    // Dialogs
    if (showEntryDialog) {
        EntryDialog(
            initialCustomer = editingFlat,
            allCustomers = customers,
            isNewEntry = isNewEntryMode,
            onDismiss = { viewModel.showEntryDialog.value = false },
            onSave = { updated -> viewModel.saveCustomerEntry(updated) }
        )
    }

    if (showPaymentDialog) {
        PaymentDialog(
            initialCustomer = paymentTargetFlat,
            allCustomers = customers,
            onDismiss = { viewModel.showPaymentDialog.value = false },
            onSavePayment = { flat, amount, date, method, remarks ->
                viewModel.recordPayment(flat, amount, date, method, remarks)
            }
        )
    }

    if (showReportDialog) {
        ReportDialog(
            billingMonth = billingMonth,
            stats = stats,
            onDismiss = { viewModel.showReportDialog.value = false }
        )
    }

    if (showBackupDialog) {
        BackupRestoreDialog(
            onDismiss = { viewModel.showBackupDialog.value = false },
            onGetJson = { viewModel.getExportJson() },
            onImportJson = { json -> viewModel.importJson(json) }
        )
    }

    if (showHistoryDialog) {
        PaymentHistoryDialog(
            payments = payments,
            onDismiss = { viewModel.showHistoryDialog.value = false }
        )
    }

    if (showAdminLoginDialog) {
        AdminLoginDialog(
            errorMessage = loginError,
            onDismiss = { viewModel.showAdminLoginDialog.value = false },
            onLogin = { email, pass -> viewModel.loginAdmin(email, pass) }
        )
    }
}

@Composable
private fun HeaderSection(
    billingMonth: String,
    isAdmin: Boolean,
    onMonthChange: (String) -> Unit,
    onAdminClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Apartment,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Jamila Bhavan-1",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gas Meter Management System",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }

                // Admin Status Chip
                AssistChip(
                    onClick = onAdminClick,
                    label = {
                        Text(
                            if (isAdmin) "Admin" else "Login",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (isAdmin) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isAdmin) Color(0xFFD1FAE5) else Color.White
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        labelColor = Color.White
                    ),
                    border = null
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Model City, Mouchak, Kaliakair, Gazipur",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                )

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(billingMonth, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSection(stats: DashboardStats) {
    Column(modifier = Modifier.padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardCard(
                title = "Total Flats",
                value = stats.totalFlats.toString(),
                icon = Icons.Default.Home,
                modifier = Modifier.weight(1f)
            )
            DashboardCard(
                title = "Total Units",
                value = "%.1f".format(stats.totalUnits),
                icon = Icons.Default.LocalFireDepartment,
                iconColor = FlameOrange,
                modifier = Modifier.weight(1f)
            )
            DashboardCard(
                title = "Total Bill",
                value = "${"%.0f".format(stats.totalBill)} ৳",
                icon = Icons.Default.Payments,
                iconColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardCard(
                title = "Paid (${stats.paidCustomers})",
                value = "${"%.0f".format(stats.totalPaid)} ৳",
                icon = Icons.Default.CheckCircle,
                iconColor = StatusPaid,
                modifier = Modifier.weight(1f)
            )
            DashboardCard(
                title = "Due (${stats.dueCustomers})",
                value = "${"%.0f".format(stats.totalDue)} ৳",
                icon = Icons.Default.ErrorOutline,
                iconColor = StatusDue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickMenuSection(
    onNewEntry: () -> Unit,
    onUpdate: () -> Unit,
    onPayment: () -> Unit,
    onReport: () -> Unit,
    onHistory: () -> Unit,
    onBackup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickActionButton("New Entry", Icons.Default.PersonAdd, onNewEntry)
                QuickActionButton("Update", Icons.Default.EditNote, onUpdate)
                QuickActionButton("Payment", Icons.Default.AccountBalanceWallet, onPayment)
                QuickActionButton("Report", Icons.Default.BarChart, onReport)
                QuickActionButton("History", Icons.Default.History, onHistory)
                QuickActionButton("Backup", Icons.Default.SettingsBackupRestore, onBackup)
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun SearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterStatus: String,
    onFilterStatusChange: (String) -> Unit,
    resultCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search Flat (e.g. A-2), Customer, or Mobile...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_box")
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("All", "Due", "Paid").forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { onFilterStatusChange(status) },
                        label = { Text(status) }
                    )
                }
            }

            Text(
                text = "$resultCount Flats",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun CustomerFlatCard(
    customer: CustomerFlat,
    onNewEntry: () -> Unit,
    onUpdate: () -> Unit,
    onPay: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("flat_card_${customer.flat}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top row: Flat number, Meter, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = customer.flat,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text(
                            text = customer.name.ifBlank { "Unassigned" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Meter: ${customer.meter}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Status chip
                val (badgeColor, textColor, text) = when {
                    customer.due <= 0.0 && customer.bill > 0.0 -> Triple(StatusPaid.copy(alpha = 0.15f), StatusPaid, "PAID")
                    customer.due > 0.0 && customer.paid > 0.0 -> Triple(StatusPartial.copy(alpha = 0.15f), StatusPartial, "PARTIAL")
                    customer.due > 0.0 -> Triple(StatusDue.copy(alpha = 0.15f), StatusDue, "DUE")
                    else -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "ACTIVE")
                }

                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Middle stats grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Readings (P / C)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${"%.0f".format(customer.previous)} → ${"%.0f".format(customer.current)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Units", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${"%.1f".format(customer.unit)} u", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Total Bill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("${"%.2f".format(customer.bill)} ৳", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        "${"%.2f".format(customer.due)} ৳",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (customer.due > 0.0) StatusDue else StatusPaid
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Action Buttons: WhatsApp Send, New Entry, Update, Pay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Send Button (Matches the original web button)
                FilledTonalButton(
                    onClick = onSendWhatsApp,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF25D366).copy(alpha = 0.15f),
                        contentColor = Color(0xFF15803D)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Bill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNewEntry,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("New", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onUpdate,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Update", fontSize = 11.sp)
                }

                Button(
                    onClick = onPay,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(Icons.Default.Wallet, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Pay", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun sendWhatsAppBill(context: Context, customer: CustomerFlat) {
    if (customer.mobile.isBlank()) {
        Toast.makeText(context, "Mobile number not found for Flat ${customer.flat}", Toast.LENGTH_SHORT).show()
        return
    }

    val customerName = customer.name.ifBlank { "Customer" }
    val message = """
JAMILA BHAVAN
Gas Meter Bill

Dear $customerName,

Flat No     : ${customer.flat}
Meter No    : ${customer.meter}
Total Unit  : ${"%.2f".format(customer.unit)}
Total Bill  : ${"%.2f".format(customer.bill)} Tk
Paid Amount : ${"%.2f".format(customer.paid)} Tk
Current Due : ${"%.2f".format(customer.due)} Tk

Please pay your due amount at your earliest convenience.

Thank you.

Jamila Bhavan Management
    """.trimIndent()

    val cleanPhone = customer.mobile.replace(Regex("\\D"), "")
    val phoneWithCountry = if (cleanPhone.startsWith("88")) cleanPhone else "88$cleanPhone"
    val whatsappUri = Uri.parse("https://wa.me/$phoneWithCountry?text=${Uri.encode(message)}")

    val intent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web browser or generic share
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, whatsappUri)
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Send Bill Details"))
        }
    }
}
