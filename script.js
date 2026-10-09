/**
 * Jamila Bhavan Gas Meter Management System
 * Full standalone Web Version with LocalStorage, WhatsApp sharing & A4 Print
 */

// 28 Fixed Flats in Jamila Bhavan with exact Meter Numbers
const FIXED_FLATS = [
  { flat: "A-2", meter: "261617108356" },
  { flat: "A-3", meter: "261617108354" },
  { flat: "A-4", meter: "261617108357" },
  { flat: "A-5", meter: "261617108352" },
  { flat: "A-6", meter: "261617108351" },
  { flat: "A-7", meter: "261617108350" },
  { flat: "A-8", meter: "261617108355" },

  { flat: "B-2", meter: "261617108343" },
  { flat: "B-4", meter: "261617108346" },
  { flat: "B-5", meter: "261617108348" },
  { flat: "B-6", meter: "261617108344" },
  { flat: "B-7", meter: "261617108345" },
  { flat: "B-8", meter: "261617108349" },

  { flat: "C-2", meter: "261617108360" },
  { flat: "C-3", meter: "261617108358" },
  { flat: "C-4", meter: "261617108361" },
  { flat: "C-5", meter: "261617108342" },
  { flat: "C-6", meter: "261617108359" },
  { flat: "C-7", meter: "261617108353" },
  { flat: "C-8", meter: "261617108347" },

  { flat: "D-1", meter: "261617108340" },
  { flat: "D-2", meter: "261617108335" },
  { flat: "D-3", meter: "261617108336" },
  { flat: "D-4", meter: "261617108338" },
  { flat: "D-5", meter: "261617108339" },
  { flat: "D-6", meter: "261617108334" },
  { flat: "D-7", meter: "261617108341" },
  { flat: "D-8", meter: "261617108337" }
];

const STORAGE_KEY = "jamila_bhavan_gas_data_v1";

// Month Navigation State
const MONTHS = [
  "January", "February", "March", "April", "May", "June", 
  "July", "August", "September", "October", "November", "December"
];
let currentYear = 2026;
let currentMonthIndex = 8; // September (0-based)

// State: { "September 2026": { "A-8": { ...record } } }
let appDatabase = {};

// Initialize application
document.addEventListener("DOMContentLoaded", () => {
  loadDataFromStorage();
  updateMonthDisplay();
  setupEventListeners();
  renderApp();
});

function getBillingMonthString() {
  return `${MONTHS[currentMonthIndex]} ${currentYear}`;
}

function loadDataFromStorage() {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved) {
    try {
      appDatabase = JSON.parse(saved);
    } catch (e) {
      console.error("Could not parse storage, using fresh database", e);
      appDatabase = {};
    }
  }
}

function saveDataToStorage() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(appDatabase));
}

function getMonthData(monthStr) {
  if (!appDatabase[monthStr]) {
    appDatabase[monthStr] = {};
  }
  return appDatabase[monthStr];
}

function getCustomerRecord(flatNumber, monthStr = getBillingMonthString()) {
  const mData = getMonthData(monthStr);
  if (!mData[flatNumber]) {
    const flatMeta = FIXED_FLATS.find(f => f.flat === flatNumber);
    mData[flatNumber] = {
      flat: flatNumber,
      meter: flatMeta ? flatMeta.meter : "",
      name: "",
      mobile: "",
      previous: 0.0,
      current: 0.0,
      unit: 0.0,
      gasRate: 290.0,
      serviceCharge: 0.0,
      previousDue: 0.0,
      lateFee: 0.0,
      discount: 0.0,
      bill: 0.0,
      paid: 0.0
    };
  }
  return mData[flatNumber];
}

function formatCurrency(val) {
  const num = parseFloat(val) || 0.0;
  return "৳" + num.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function updateMonthDisplay() {
  document.getElementById("currentMonthLabel").textContent = getBillingMonthString();
}

function setupEventListeners() {
  // Month controls
  document.getElementById("prevMonthBtn").addEventListener("click", () => {
    currentMonthIndex--;
    if (currentMonthIndex < 0) {
      currentMonthIndex = 11;
      currentYear--;
    }
    updateMonthDisplay();
    renderApp();
  });

  document.getElementById("nextMonthBtn").addEventListener("click", () => {
    currentMonthIndex++;
    if (currentMonthIndex > 11) {
      currentMonthIndex = 0;
      currentYear++;
    }
    updateMonthDisplay();
    renderApp();
  });

  // Search & Filter
  document.getElementById("searchInput").addEventListener("input", renderApp);
  document.getElementById("statusFilter").addEventListener("change", renderApp);

  // Entry Form Real-time calculation
  const currInput = document.getElementById("entryCurrent");
  const prevInput = document.getElementById("entryPrevious");
  const rateInput = document.getElementById("entryGasRate");
  const prevDueInput = document.getElementById("entryPrevDue");
  const scInput = document.getElementById("entryServiceCharge");
  const lateInput = document.getElementById("entryLateFee");
  const discInput = document.getElementById("entryDiscount");

  [currInput, prevInput, rateInput, prevDueInput, scInput, lateInput, discInput].forEach(inp => {
    inp.addEventListener("input", recalculateModalSummary);
  });

  // Entry Save
  document.getElementById("entryForm").addEventListener("submit", handleEntrySave);

  // Payment Save
  document.getElementById("paymentForm").addEventListener("submit", handlePaymentSave);

  // Backup & Restore
  document.getElementById("backupBtn").addEventListener("click", () => {
    openModal("backupModal");
  });

  document.getElementById("exportDataBtn").addEventListener("click", exportBackupJSON);
  document.getElementById("importFileInput").addEventListener("change", importBackupJSON);

  // Print
  document.getElementById("printReportBtn").addEventListener("click", handlePrintSheet);
}

function recalculateModalSummary() {
  const current = parseFloat(document.getElementById("entryCurrent").value) || 0.0;
  const previous = parseFloat(document.getElementById("entryPrevious").value) || 0.0;
  const gasRate = parseFloat(document.getElementById("entryGasRate").value) || 290.0;
  const previousDue = parseFloat(document.getElementById("entryPrevDue").value) || 0.0;
  const serviceCharge = parseFloat(document.getElementById("entryServiceCharge").value) || 0.0;
  const lateFee = parseFloat(document.getElementById("entryLateFee").value) || 0.0;
  const discount = parseFloat(document.getElementById("entryDiscount").value) || 0.0;

  const unit = Math.max(0.0, current - previous);
  document.getElementById("entryUnit").value = unit.toFixed(2);

  const gasBill = unit * gasRate;
  const totalBill = Math.max(0.0, gasBill + serviceCharge + previousDue + lateFee - discount);

  document.getElementById("calcGasBill").textContent = formatCurrency(gasBill);
  document.getElementById("calcTotalBill").textContent = formatCurrency(totalBill);
}

function renderApp() {
  const currentMonthStr = getBillingMonthString();
  const search = document.getElementById("searchInput").value.trim().toLowerCase();
  const statusFilter = document.getElementById("statusFilter").value;

  const tbody = document.getElementById("customerTableBody");
  tbody.innerHTML = "";

  let totalUnits = 0.0;
  let totalBilled = 0.0;
  let totalPaid = 0.0;
  let totalDue = 0.0;
  let visibleCount = 0;

  FIXED_FLATS.forEach(meta => {
    const cust = getCustomerRecord(meta.flat, currentMonthStr);

    const unit = cust.unit > 0.0 ? cust.unit : Math.max(0.0, cust.current - cust.previous);
    const gasBill = unit * cust.gasRate;
    const totalBill = cust.bill > 0.0 ? cust.bill : Math.max(0.0, gasBill + cust.serviceCharge + cust.previousDue + cust.lateFee - cust.discount);
    const currentDue = Math.max(0.0, totalBill - cust.paid);

    totalUnits += unit;
    totalBilled += totalBill;
    totalPaid += cust.paid;
    totalDue += currentDue;

    // Filter checks
    const matchSearch = cust.flat.toLowerCase().includes(search) || 
                        cust.name.toLowerCase().includes(search) ||
                        cust.meter.toLowerCase().includes(search);

    if (!matchSearch) return;

    if (statusFilter === "DUE" && currentDue <= 0.0) return;
    if (statusFilter === "PAID" && (currentDue > 0.0 || totalBill === 0.0)) return;

    visibleCount++;

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><span class="flat-pill">${cust.flat}</span></td>
      <td>
        <strong>${cust.name || "—"}</strong><br>
        <small style="color:var(--text-muted);">${cust.mobile || "No Mobile"}</small>
      </td>
      <td><code>${cust.meter}</code></td>
      <td>${cust.previous.toFixed(2)} / ${cust.current.toFixed(2)}</td>
      <td><strong>${unit.toFixed(2)}</strong></td>
      <td>${formatCurrency(gasBill)}</td>
      <td>${formatCurrency(cust.previousDue)}</td>
      <td><strong>${formatCurrency(totalBill)}</strong></td>
      <td class="text-success">${formatCurrency(cust.paid)}</td>
      <td class="${currentDue > 0 ? 'text-danger' : 'text-success'}">
        ${formatCurrency(currentDue)}
      </td>
      <td class="actions-col">
        <div class="action-btns">
          <button class="btn btn-outline btn-sm" onclick="openEntryModal('${cust.flat}')" title="Edit Meter Reading">✏️ Entry</button>
          <button class="btn btn-success btn-sm" onclick="openPaymentModal('${cust.flat}')" title="Collect Payment">💵 Pay</button>
          <button class="btn btn-whatsapp btn-sm" onclick="sendWhatsAppBill('${cust.flat}')" title="Send WhatsApp Bill">💬 Bill</button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });

  // Update Statistics
  document.getElementById("statTotalFlats").textContent = FIXED_FLATS.length;
  document.getElementById("statTotalUnits").textContent = totalUnits.toFixed(2);
  document.getElementById("statTotalBill").textContent = formatCurrency(totalBilled);
  document.getElementById("statTotalPaid").textContent = formatCurrency(totalPaid);
  document.getElementById("statTotalDue").textContent = formatCurrency(totalDue);
  document.getElementById("flatCountBadge").textContent = `Showing: ${visibleCount} of ${FIXED_FLATS.length} flats`;
}

// Modal functions
function openModal(id) {
  document.getElementById(id).classList.add("active");
}

function closeModal(id) {
  document.getElementById(id).classList.remove("active");
}

function openEntryModal(flat) {
  const cust = getCustomerRecord(flat);
  document.getElementById("modalTitle").textContent = `Meter Reading - Flat ${flat}`;
  document.getElementById("entryFlat").value = flat;
  document.getElementById("entryName").value = cust.name;
  document.getElementById("entryMobile").value = cust.mobile;
  document.getElementById("entryMeter").value = cust.meter;
  document.getElementById("entryGasRate").value = cust.gasRate || 290.0;
  document.getElementById("entryPrevious").value = cust.previous;
  document.getElementById("entryCurrent").value = cust.current;
  document.getElementById("entryPrevDue").value = cust.previousDue;
  document.getElementById("entryServiceCharge").value = cust.serviceCharge;
  document.getElementById("entryLateFee").value = cust.lateFee;
  document.getElementById("entryDiscount").value = cust.discount;

  recalculateModalSummary();
  openModal("entryModal");
}

function handleEntrySave(e) {
  e.preventDefault();
  const flat = document.getElementById("entryFlat").value;
  const cust = getCustomerRecord(flat);

  cust.name = document.getElementById("entryName").value.trim();
  cust.mobile = document.getElementById("entryMobile").value.trim();
  cust.gasRate = parseFloat(document.getElementById("entryGasRate").value) || 290.0;
  cust.previous = parseFloat(document.getElementById("entryPrevious").value) || 0.0;
  cust.current = parseFloat(document.getElementById("entryCurrent").value) || 0.0;
  cust.unit = Math.max(0.0, cust.current - cust.previous);
  cust.previousDue = parseFloat(document.getElementById("entryPrevDue").value) || 0.0;
  cust.serviceCharge = parseFloat(document.getElementById("entryServiceCharge").value) || 0.0;
  cust.lateFee = parseFloat(document.getElementById("entryLateFee").value) || 0.0;
  cust.discount = parseFloat(document.getElementById("entryDiscount").value) || 0.0;

  const gasBill = cust.unit * cust.gasRate;
  cust.bill = Math.max(0.0, gasBill + cust.serviceCharge + cust.previousDue + cust.lateFee - cust.discount);

  saveDataToStorage();
  closeModal("entryModal");
  renderApp();
}

function openPaymentModal(flat) {
  const cust = getCustomerRecord(flat);
  const unit = cust.unit > 0.0 ? cust.unit : Math.max(0.0, cust.current - cust.previous);
  const gasBill = unit * cust.gasRate;
  const totalBill = cust.bill > 0.0 ? cust.bill : Math.max(0.0, gasBill + cust.serviceCharge + cust.previousDue + cust.lateFee - cust.discount);
  const currentDue = Math.max(0.0, totalBill - cust.paid);

  document.getElementById("payModalTitle").textContent = `Receive Payment - Flat ${flat}`;
  document.getElementById("payFlat").value = flat;
  document.getElementById("payTotalBill").value = formatCurrency(totalBill);
  document.getElementById("payAlreadyPaid").value = formatCurrency(cust.paid);
  document.getElementById("payCurrentDue").value = formatCurrency(currentDue);
  document.getElementById("payAmountInput").value = currentDue > 0 ? currentDue : "";

  openModal("paymentModal");
}

function handlePaymentSave(e) {
  e.preventDefault();
  const flat = document.getElementById("payFlat").value;
  const cust = getCustomerRecord(flat);
  const paymentAmount = parseFloat(document.getElementById("payAmountInput").value) || 0.0;

  if (paymentAmount <= 0) return;

  cust.paid += paymentAmount;
  saveDataToStorage();
  closeModal("paymentModal");
  renderApp();
}

/**
 * EXACT CLEAN & MOBILE-FRIENDLY WHATSAPP BILL FORMAT
 */
function sendWhatsAppBill(flat) {
  const billingMonth = getBillingMonthString();
  const customer = getCustomerRecord(flat);

  const tenantName = customer.name.trim() || "Tenant";
  const unit = customer.unit > 0.0 
    ? customer.unit 
    : Math.max(0.0, customer.current - customer.previous);

  const gasBill = unit * customer.gasRate;
  const totalBill = customer.bill > 0.0 
    ? customer.bill 
    : Math.max(0.0, gasBill + customer.serviceCharge + customer.previousDue + customer.lateFee - customer.discount);

  const formattedUnit = unit.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const formattedGasBill = gasBill.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const formattedPrevDue = customer.previousDue.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const formattedTotalBill = totalBill.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  // EXACT FORMAT AS SPECIFIED
  const message = `━━━━━━━━━━━━━━━━━━
JAMILA BHAVAN
GAS BILL
━━━━━━━━━━━━━━━━━━

Dear ${tenantName},

Flat: ${customer.flat}
Meter: ${customer.meter}
Billing Month: ${billingMonth}

Total Unit: ${formattedUnit}
Gas Bill: ৳${formattedGasBill}
Previous Due: ৳${formattedPrevDue}

*💵 TOTAL BILL: ৳${formattedTotalBill}*

Please pay the bill within the due time.

Thank you.
━━━━━━━━━━━━━━━━━━`;

  let cleanPhone = customer.mobile.replace(/[^0-9]/g, "");
  if (cleanPhone.startsWith("0")) {
    cleanPhone = "88" + cleanPhone;
  }

  const encodedMsg = encodeURIComponent(message);
  const waUrl = cleanPhone 
    ? `https://api.whatsapp.com/send?phone=${cleanPhone}&text=${encodedMsg}`
    : `https://api.whatsapp.com/send?text=${encodedMsg}`;

  window.open(waUrl, "_blank");
}

// Backup & Restore
function exportBackupJSON() {
  const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(appDatabase, null, 2));
  const downloadAnchor = document.createElement("a");
  downloadAnchor.setAttribute("href", dataStr);
  downloadAnchor.setAttribute("download", `jamila_bhavan_gas_backup_${new Date().toISOString().slice(0,10)}.json`);
  document.body.appendChild(downloadAnchor);
  downloadAnchor.click();
  downloadAnchor.remove();
}

function importBackupJSON(e) {
  const file = e.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = function(event) {
    try {
      const parsed = JSON.parse(event.target.result);
      if (typeof parsed === "object") {
        appDatabase = parsed;
        saveDataToStorage();
        alert("Backup successfully restored!");
        closeModal("backupModal");
        renderApp();
      }
    } catch (err) {
      alert("Invalid backup file: " + err.message);
    }
  };
  reader.readAsText(file);
}

// A4 Print Handler
function handlePrintSheet() {
  const currentMonthStr = getBillingMonthString();
  document.getElementById("printMonthText").textContent = `Month: ${currentMonthStr}`;
  const table = document.getElementById("printTable");

  let html = `
    <thead>
      <tr>
        <th>Flat</th>
        <th>Tenant</th>
        <th>Meter</th>
        <th>Prev</th>
        <th>Curr</th>
        <th>Unit</th>
        <th>Gas Bill (৳)</th>
        <th>Prev Due (৳)</th>
        <th>Total Bill (৳)</th>
        <th>Paid (৳)</th>
        <th>Due (৳)</th>
      </tr>
    </thead>
    <tbody>
  `;

  FIXED_FLATS.forEach(meta => {
    const cust = getCustomerRecord(meta.flat, currentMonthStr);
    const unit = cust.unit > 0.0 ? cust.unit : Math.max(0.0, cust.current - cust.previous);
    const gasBill = unit * cust.gasRate;
    const totalBill = cust.bill > 0.0 ? cust.bill : Math.max(0.0, gasBill + cust.serviceCharge + cust.previousDue + cust.lateFee - cust.discount);
    const currentDue = Math.max(0.0, totalBill - cust.paid);

    html += `
      <tr>
        <td><strong>${cust.flat}</strong></td>
        <td>${cust.name || "—"}</td>
        <td>${cust.meter}</td>
        <td>${cust.previous.toFixed(2)}</td>
        <td>${cust.current.toFixed(2)}</td>
        <td>${unit.toFixed(2)}</td>
        <td>${gasBill.toFixed(2)}</td>
        <td>${cust.previousDue.toFixed(2)}</td>
        <td><strong>${totalBill.toFixed(2)}</strong></td>
        <td>${cust.paid.toFixed(2)}</td>
        <td>${currentDue.toFixed(2)}</td>
      </tr>
    `;
  });

  html += "</tbody>";
  table.innerHTML = html;
  window.print();
}
