// ======================================
// Jamila Bhavan
// Gas Meter Management System
// script.js Version 2.0
// Part - 1
// ======================================


// ===============================
// SYSTEM SETTINGS
// ===============================

let GAS_RATE = 290;
let SERVICE_CHARGE = 0;


// ===============================
// FIXED FLAT LIST (28)
// ===============================

const FLATS = [

"A-2","A-3","A-4","A-5","A-6","A-7","A-8",

"B-2","B-4","B-5","B-6","B-7","B-8",

"C-2","C-3","C-4","C-5","C-6","C-7","C-8",

"D-1","D-2","D-3","D-4","D-5","D-6","D-7","D-8"

];


// ===============================
// FIXED GAS METER NUMBER
// ===============================

const METERS = {

"A-2":"261617108356",
"A-3":"261617108354",
"A-4":"261617108357",
"A-5":"261617108352",
"A-6":"261617108351",
"A-7":"261617108350",
"A-8":"261617108355",

"B-2":"261617108343",
"B-4":"261617108346",
"B-5":"261617108348",
"B-6":"261617108344",
"B-7":"261617108345",
"B-8":"261617108349",

"C-2":"261617108360",
"C-3":"261617108358",
"C-4":"261617108361",
"C-5":"261617108342",
"C-6":"261617108359",
"C-7":"261617108353",
"C-8":"261617108347",

"D-1":"261617108340",
"D-2":"261617108335",
"D-3":"261617108336",
"D-4":"261617108338",
"D-5":"261617108339",
"D-6":"261617108334",
"D-7":"261617108341",
"D-8":"261617108337"

};


// ===============================
// DATABASE
// ===============================


let customers = JSON.parse(

localStorage.getItem("customers")

) || [];



let paymentHistory = JSON.parse(

localStorage.getItem("paymentHistory")

) || [];



// ===============================
// HTML ELEMENT
// ===============================


const tableBody =
document.querySelector("#customerTable tbody");


const flatSelect =
document.getElementById("flatNo");


const meterInput =
document.getElementById("meterNo");


const searchInput =
document.getElementById("searchBox");



// ===============================
// SAVE DATABASE
// ===============================


function saveData(){

localStorage.setItem(

"customers",

JSON.stringify(customers)

);


localStorage.setItem(

"paymentHistory",

JSON.stringify(paymentHistory)

);

}



// ===============================
// LOAD FLAT LIST
// ===============================


function loadFlats(){


flatSelect.innerHTML="";


FLATS.forEach(flat=>{


let option =
document.createElement("option");


option.value = flat;


option.textContent = flat;


flatSelect.appendChild(option);


});



meterInput.value =
METERS[flatSelect.value];


}




// ===============================
// FLAT CHANGE EVENT
// ===============================


flatSelect.addEventListener(

"change",

function(){


meterInput.value =
METERS[this.value];


}

);



// ===============================
// CREATE INITIAL 28 DATA
// ===============================


function createDefaultCustomers(){


if(customers.length > 0){

return;

}



FLATS.forEach(flat=>{


customers.push({


flat:flat,

meter:METERS[flat],

name:"",

mobile:"",

previous:0,

current:0,

unit:0,

bill:0,

paid:0,

due:0,

status:"Active"


});


});



saveData();


}


// ===============================
// START SYSTEM
// ===============================


createDefaultCustomers();


loadFlats();

// ======================================
// Jamila Bhavan
// Gas Meter Management System
// script.js Version 2.0
// Part - 2
// ======================================



// ===============================
// RENDER CUSTOMER TABLE
// ===============================


function renderTable(){

    tableBody.innerHTML = "";

    customers.forEach((customer,index)=>{

        tableBody.innerHTML += `

        <tr data-index="${index}">

            <td>${customer.flat}</td>

            <td>${customer.meter}</td>

            <td>${customer.name || "-"}</td>

            <td>${customer.mobile || "-"}</td>

            <td>${customer.previous}</td>

            <td>${customer.current}</td>

            <td>${customer.unit}</td>

            <td>${Number(customer.bill || 0).toFixed(2)}</td>

            <td>${Number(customer.paid || 0).toFixed(2)}</td>

            <td>${Number(customer.due || 0).toFixed(2)}</td>

            <td>${customer.status}</td>

            <td>
                <button
                    class="sendBtn"
                    onclick="event.stopPropagation(); sendCustomerWhatsApp(${index})">
                    <i class="fab fa-whatsapp"></i>
                    Send
                </button>
            </td>

        </tr>

        `;

    });

}




// ===============================
// UPDATE DASHBOARD
// ===============================


function updateDashboard(){



let totalUnit = 0;

let totalBill = 0;

let totalPaid = 0;

let totalDue = 0;



customers.forEach(customer=>{


totalUnit += Number(customer.unit);


totalBill += Number(customer.bill);


totalPaid += Number(customer.paid);


totalDue += Number(customer.due);



});





document.getElementById("totalFlat").innerText =

FLATS.length;



document.getElementById("totalUnit").innerText =

totalUnit;



document.getElementById("totalBill").innerText =

totalBill.toFixed(2);



document.getElementById("paidBill").innerText =

totalPaid.toFixed(2);



document.getElementById("dueBill").innerText =

totalDue.toFixed(2);



}





// ==========================================
// SEARCH SYSTEM
// ==========================================

const searchInput =
    document.getElementById("searchBox");


if (searchInput) {

    searchInput.addEventListener(
        "input",
        function () {

            const value =
                this.value.toLowerCase().trim();


            const rows =
                document.querySelectorAll(
                    "#customerTable tbody tr"
                );


            rows.forEach(function (row) {

                const text =
                    row.innerText.toLowerCase();


                if (text.includes(value)) {

                    row.style.display = "";

                }

                else {

                    row.style.display = "none";

                }

            });

        }
    );

}
// ===============================
// SELECT CUSTOMER ROW
// ===============================

// ===============================
// SELECT CUSTOMER ROW
// ===============================

document.querySelector("#customerTable tbody")
.addEventListener("click", function(e){

    let row = e.target.closest("tr");

    if(!row){
        return;
    }

   selectedIndex = Number(row.dataset.index);

if(window.innerWidth <= 600){

    return;

}

let customer = customers[selectedIndex];

});


// ===============================
// INITIAL LOAD
// ===============================


renderTable();

updateDashboard();

// ======================================
// Jamila Bhavan
// Gas Meter Management System
// script.js Version 2.0
// Part - 3
// New Entry & Bill Calculation
// ======================================



// ===============================
// SELECTED INDEX
// ===============================

let selectedIndex = -1;




// ===============================
// OPEN ENTRY MODAL
// ===============================


document.getElementById("addCustomerBtn")
.onclick = function(){


selectedIndex = -1;


document.getElementById("entryModal")
.style.display = "flex";



};






// ===============================
// AUTO BILL CALCULATION
// ===============================


function calculateBill(){



let previous = Number(

document.getElementById("previousReading").value || 0

);



let current = Number(

document.getElementById("currentReading").value || 0

);



let rate = Number(

document.getElementById("gasRate").value || GAS_RATE

);



let service = Number(

document.getElementById("serviceCharge").value || 0

);



let previousDue = Number(

document.getElementById("previousDue").value || 0

);



let discount = Number(

document.getElementById("discount").value || 0

);



let lateFee = Number(

document.getElementById("lateFee").value || 0

);



let received = Number(

document.getElementById("receivedAmount").value || 0

);





let unit = current - previous;



if(unit < 0){

unit = 0;

}





let total =

(unit * rate)

+ service

+ previousDue

+ lateFee

- discount;





let due = total - received;





document.getElementById("totalAmount").value =

total.toFixed(2);



document.getElementById("currentDue").value =

due.toFixed(2);



return {

unit,

total,

due

};


}






// ===============================
// AUTO CALCULATE WHEN INPUT CHANGE
// ===============================


[
"previousReading",
"currentReading",
"gasRate",
"serviceCharge",
"previousDue",
"discount",
"lateFee",
"receivedAmount"

].forEach(id=>{


document.getElementById(id)

.addEventListener(

"input",

calculateBill

);


});






// ===============================
// SAVE ENTRY
// ===============================


document.getElementById("entryForm")

.addEventListener(

"submit",

function(e){


e.preventDefault();




let flat =

document.getElementById("flatNo").value;



let index =

customers.findIndex(

c=>c.flat === flat

);



if(index === -1){

alert("Flat not found");

return;

}





let result = calculateBill();





customers[index].name =

document.getElementById("customerName").value;



customers[index].mobile =

document.getElementById("mobileNumber").value;



customers[index].previous =

Number(document.getElementById("previousReading").value);



customers[index].current =

Number(document.getElementById("currentReading").value);



customers[index].unit =

result.unit;



customers[index].bill =

result.total;



customers[index].paid =

Number(document.getElementById("receivedAmount").value || 0);



customers[index].due =

result.due;



customers[index].status =

result.due <= 0 ? "Paid" : "Due";





saveData();


renderTable();


updateDashboard();




alert("Entry Saved Successfully");



document.getElementById("entryModal")

.style.display="none";



});


// ======================================
// Jamila Bhavan
// Gas Meter Management System
// script.js Version 2.0
// Part - 4
// Edit / Update / Delete / Modal Control
// ======================================



// ===============================
// LOAD CUSTOMER DATA FOR EDIT
// ===============================


document.querySelector("#customerTable tbody")
.addEventListener("click",function(e){


let row = e.target.closest("tr");


if(!row){

return;

}


selectedIndex = Number(row.dataset.index);

if(window.innerWidth <= 600){
    return;
}

let customer = customers[selectedIndex];



document.getElementById("flatNo").value =
customer.flat;


document.getElementById("meterNo").value =
customer.meter;


document.getElementById("customerName").value =
customer.name;


document.getElementById("mobileNumber").value =
customer.mobile;


document.getElementById("previousReading").value =
customer.previous;


document.getElementById("currentReading").value =
customer.current;


document.getElementById("gasRate").value =
GAS_RATE;


document.getElementById("serviceCharge").value =
SERVICE_CHARGE;


document.getElementById("totalAmount").value =
customer.bill;


document.getElementById("receivedAmount").value =
customer.paid;


document.getElementById("currentDue").value =
customer.due;



document.getElementById("entryModal")
.style.display="flex";


});






// ===============================
// UPDATE CUSTOMER
// ===============================


document.getElementById("updateBtn")
.onclick=function(){



if(selectedIndex === -1){


alert("Please select customer first");


return;


}




let result = calculateBill();



customers[selectedIndex].name =

document.getElementById("customerName").value;



customers[selectedIndex].mobile =

document.getElementById("mobileNumber").value;



customers[selectedIndex].previous =

Number(document.getElementById("previousReading").value);



customers[selectedIndex].current =

Number(document.getElementById("currentReading").value);



customers[selectedIndex].unit =

result.unit;



customers[selectedIndex].bill =

result.total;



customers[selectedIndex].paid =

Number(document.getElementById("receivedAmount").value || 0);



customers[selectedIndex].due =

result.due;



customers[selectedIndex].status =

result.due <= 0 ? "Paid" : "Due";





saveData();


renderTable();


updateDashboard();



alert("Customer Updated Successfully");



};







// ===============================
// DELETE CUSTOMER DATA
// Meter Number Will Remain Fixed
// ===============================


document.getElementById("deleteBtn")
.onclick=function(){



if(selectedIndex === -1){


alert("Please select customer first");


return;


}




if(confirm("Clear this customer data?")){



let meter =
customers[selectedIndex].meter;


let flat =
customers[selectedIndex].flat;




customers[selectedIndex]={


flat:flat,


meter:meter,


name:"",


mobile:"",


previous:0,


current:0,


unit:0,


bill:0,


paid:0,


due:0,


status:"Active"



};





saveData();


renderTable();


updateDashboard();



selectedIndex=-1;



alert("Customer Data Cleared");


}



};






// ===============================
// RESET FORM
// ===============================


function resetForm(){



document.getElementById("entryForm")
.reset();



document.getElementById("meterNo").value =

METERS[

document.getElementById("flatNo").value

];



selectedIndex=-1;



}






// ===============================
// CANCEL BUTTON
// ===============================


document.getElementById("cancelBtn")
.onclick=function(){


document.getElementById("entryModal")
.style.display="none";


resetForm();


};






// ===============================
// CLOSE (X) BUTTON
// ===============================


document.querySelector(".close")
.onclick=function(){


document.getElementById("entryModal")
.style.display="none";


resetForm();


};

// ======================================
// Jamila Bhavan
// Gas Meter Management System
// script.js Version 2.0
// Part - 5
// Payment System
// ======================================



// ===============================
// OPEN PAYMENT MODAL
// ===============================


document.getElementById("paymentBtn")
.onclick=function(){


document.getElementById("paymentModal")
.style.display="flex";


};






// ===============================
// LOAD PAYMENT FLAT LIST
// ===============================


const paymentFlat = 
document.getElementById("paymentFlat");

console.log(paymentFlat);


if(paymentFlat){


paymentFlat.innerHTML="";


FLATS.forEach(flat=>{


let option =
document.createElement("option");


option.value = flat;


option.textContent = flat;


paymentFlat.appendChild(option);


});
console.log("Flat Loaded", paymentFlat.options.length);

}






// ===============================
// PAYMENT CUSTOMER LOAD
// ===============================


// ===============================
// PAYMENT CUSTOMER & DUE LOAD
// ===============================

if(paymentFlat){

paymentFlat.onchange = function(){

let customer = customers.find(
c => c.flat === this.value
);


if(!customer){
return;
}


// Auto Customer Name

document.getElementById("paymentCustomer").value =
customer.name || "";


// Auto Total Due

document.getElementById("paymentDue").value =
customer.due || 0;


};

}

// Load default flat data
paymentFlat.dispatchEvent(new Event("change"));





// ===============================
// SAVE PAYMENT
// ===============================


document.getElementById("paymentForm")

.addEventListener(

"submit",

function(e){



e.preventDefault();




let flat =

document.getElementById("paymentFlat").value;




let amount = Number(

document.getElementById("paymentAmount").value || 0

);




if(amount <= 0){


alert("Enter Payment Amount");


return;


}






let index = customers.findIndex(

c=>c.flat===flat

);




if(index === -1){


alert("Customer Not Found");


return;


}





customers[index].paid += amount;




customers[index].due =

customers[index].bill -

customers[index].paid;





if(customers[index].due <=0){



customers[index].due = 0;


customers[index].status = "Paid";


}

else{


customers[index].status = "Partial";


}






// ===============================
// PAYMENT HISTORY
// ===============================



paymentHistory.push({


flat:flat,


customer:customers[index].name,


amount:amount,


date:

document.getElementById("paymentDate").value,


method:

document.getElementById("paymentMethod").value,


remarks:

document.getElementById("paymentRemarks").value



});





saveData();


renderTable();


updateDashboard();





alert("Payment Saved Successfully");





document.getElementById("paymentModal")

.style.display="none";



});







// ===============================
// CLOSE PAYMENT MODAL
// ===============================


let paymentClose =

document.querySelector(".paymentClose");



if(paymentClose){


paymentClose.onclick=function(){


document.getElementById("paymentModal")

.style.display="none";


};


}






// ===============================
// PAYMENT CANCEL
// ===============================


let paymentCancel =

document.getElementById("paymentCancel");



if(paymentCancel){


paymentCancel.onclick=function(){


document.getElementById("paymentModal")

.style.display="none";


};


}

// ======================================
// Jamila Bhavan
// Gas Meter Management System
// script.js Version 2.0
// Part - 6
// Monthly Report & Print
// ======================================



// ===============================
// GENERATE REPORT
// ===============================


function generateReport(){


let totalUnit = 0;

let totalBill = 0;

let totalPaid = 0;

let totalDue = 0;


let paidCustomer = 0;

let dueCustomer = 0;



customers.forEach(customer=>{


totalUnit += Number(customer.unit);


totalBill += Number(customer.bill);


totalPaid += Number(customer.paid);


totalDue += Number(customer.due);



if(customer.due <= 0){


paidCustomer++;


}

else{


dueCustomer++;


}



});





let month =

document.getElementById("billingMonth").value;



let report = `


================================

        JAMILA BHAVAN

 Gas Meter Management System

================================


Billing Month : ${month || "Not Selected"}



Total Flat :

${FLATS.length}



Total Unit :

${totalUnit}



Total Bill :

${totalBill.toFixed(2)}



Total Paid :

${totalPaid.toFixed(2)}



Total Due :

${totalDue.toFixed(2)}



-------------------------------


Paid Customer :

${paidCustomer}



Due Customer :

${dueCustomer}



================================


Generated By:

Rezaul Haque


`;



return report;


}






// ===============================
// REPORT BUTTON
// ===============================


document.getElementById("reportBtn")
// ===============================
// REPORT BUTTON
// ===============================

document.getElementById("reportBtn").onclick = function () {

    // ===============================
    // CALCULATE REPORT DATA
    // ===============================

    let totalUnit = 0;
    let totalBill = 0;
    let totalPaid = 0;
    let totalDue = 0;

    let paidCustomer = 0;
    let dueCustomer = 0;

    customers.forEach(customer => {

        totalUnit += Number(customer.unit || 0);

        totalBill += Number(customer.bill || 0);

        totalPaid += Number(customer.paid || 0);

        totalDue += Number(customer.due || 0);

        if (Number(customer.due || 0) <= 0) {
            paidCustomer++;
        } else {
            dueCustomer++;
        }

    });


    // ===============================
    // PUT DATA INTO REPORT TABLE
    // ===============================

    document.getElementById("reportMonth").innerText =
        document.getElementById("billingMonth").value || "Not Selected";


    document.getElementById("reportFlat").innerText =
        FLATS.length;


    document.getElementById("reportUnit").innerText =
        totalUnit;


    document.getElementById("reportBill").innerText =
        totalBill.toFixed(2);


    document.getElementById("reportPaid").innerText =
        totalPaid.toFixed(2);


    document.getElementById("reportDue").innerText =
        totalDue.toFixed(2);


    // ===============================
    // ALSO GENERATE TEXT REPORT
    // ===============================

    let report = generateReport();

    let content =
        document.getElementById("billContent");

    if (content) {

        content.innerHTML =
            "<pre>" + report + "</pre>";

    }


    // ===============================
    // OPEN REPORT MODAL
    // ===============================

    document.getElementById("reportModal")
        .style.display = "flex";

};






// ===============================
// PRINT REPORT
// ===============================


document.getElementById("printBtn")

.onclick=function(){



let report = generateReport();



let win = window.open("");



win.document.write(`


<html>

<head>

<title>Jamila Bhavan Report</title>


<style>

body{

font-family:Arial;

padding:20px;

}


pre{

font-size:16px;

}

</style>


</head>


<body>


<pre>

${report}

</pre>


</body>


</html>


`);



win.document.close();


win.print();



};







// ===============================
// REPORT CLOSE
// ===============================


let reportClose =

document.querySelector(".reportClose");



if(reportClose){


reportClose.onclick=function(){


document.getElementById("reportModal")

.style.display="none";


};


}






// ===============================
// REPORT CANCEL BUTTON
// ===============================


let reportCancel =

document.getElementById("reportCancel");



if(reportCancel){


reportCancel.onclick=function(){


document.getElementById("reportModal")

.style.display="none";


};


}







// ===============================
// SAVE TEXT REPORT
// ===============================


function downloadReport(){



let text = generateReport();



let blob = new Blob(

[text],

{

type:"text/plain"

}

);



let link = document.createElement("a");



link.href = URL.createObjectURL(blob);



link.download =

"Jamila_Bhavan_Monthly_Report.txt";



link.click();



}

// ===============================
// SAVE REPORT AS PDF
// ===============================

document.getElementById("exportPDF").onclick = function () {

    const { jsPDF } = window.jspdf;

    const doc = new jsPDF();

    // ===============================
    // REPORT DATA
    // ===============================

    let month =
        document.getElementById("billingMonth").value || "Not Selected";

    let totalFlat =
        document.getElementById("reportFlat").innerText;

    let totalUnit =
        document.getElementById("reportUnit").innerText;

    let totalBill =
        document.getElementById("reportBill").innerText;

    let totalPaid =
        document.getElementById("reportPaid").innerText;

    let totalDue =
        document.getElementById("reportDue").innerText;


    // ===============================
    // PDF TITLE
    // ===============================

    doc.setFontSize(20);

    doc.text(
        "JAMILA BHAVAN",
        105,
        20,
        { align: "center" }
    );


    doc.setFontSize(13);

    doc.text(
        "Gas Meter Management System",
        105,
        29,
        { align: "center" }
    );


    doc.setFontSize(11);

    doc.text(
        "Billing Month: " + month,
        20,
        42
    );


    // ===============================
    // REPORT BOX
    // ===============================

    doc.setFontSize(12);

    doc.text(
        "Monthly Gas Meter Report",
        20,
        55
    );


    // ===============================
    // REPORT DATA
    // ===============================

    let y = 70;

    doc.text("Total Meter / Flat", 25, y);
    doc.text(String(totalFlat), 150, y);

    y += 12;

    doc.text("Total Unit", 25, y);
    doc.text(String(totalUnit), 150, y);

    y += 12;

    doc.text("Total Bill", 25, y);
    doc.text(String(totalBill), 150, y);

    y += 12;

    doc.text("Total Paid", 25, y);
    doc.text(String(totalPaid), 150, y);

    y += 12;

    doc.text("Total Due", 25, y);
    doc.text(String(totalDue), 150, y);


    // ===============================
    // FOOTER
    // ===============================

    y += 25;

    doc.text(
        "Generated by: Rezaul Haque",
        20,
        y
    );


    doc.text(
        "Jamila Bhavan - Gas Meter Management System",
        20,
        y + 8
    );


    // ===============================
    // SAVE PDF
    // ===============================

    doc.save(
        "Jamila_Bhavan_Monthly_Report_" + month + ".pdf"
    );

};

// ======================================
// Jamila Bhavan
// Gas Meter Management System
// script.js Version 2.0
// Part - 7
// Backup & Restore System
// ======================================



// ===============================
// EXPORT BACKUP
// ===============================


function exportBackup(){


let backupData = {


building:"Jamila Bhavan",


date:new Date().toLocaleString(),


customers:customers,


paymentHistory:paymentHistory



};



let file = new Blob(

[JSON.stringify(backupData,null,2)],

{

type:"application/json"

}

);



let link = document.createElement("a");



link.href = URL.createObjectURL(file);



link.download =

"Jamila_Bhavan_Backup.json";



link.click();



}







// ===============================
// IMPORT BACKUP
// ===============================


function importBackup(file){



let reader = new FileReader();



reader.onload=function(e){



try{


let data = JSON.parse(

e.target.result

);



if(data.customers){



customers = data.customers;



}



if(data.paymentHistory){



paymentHistory = data.paymentHistory;



}



saveData();



renderTable();



updateDashboard();



alert(

"Backup Restored Successfully"

);



}



catch(error){



alert(

"Invalid Backup File"

);



}



};



reader.readAsText(file);



}







// ===============================
// RESET DATABASE
// ===============================


function resetDatabase(){



let confirmDelete = confirm(

"All customer data will be cleared. Continue?"

);



if(!confirmDelete){

return;

}




localStorage.removeItem(

"customers"

);



localStorage.removeItem(

"paymentHistory"

);



customers=[];


paymentHistory=[];



createDefaultCustomers();



renderTable();



updateDashboard();



alert(

"Database Reset Completed"

);



}







// ===============================
// EXPORT CUSTOMER LIST
// ===============================


function exportCustomerList(){



let text = "";


text +=

"Jamila Bhavan Gas Meter Report\n\n";



customers.forEach(customer=>{



text +=

`
Flat: ${customer.flat}

Meter: ${customer.meter}

Customer: ${customer.name}

Unit: ${customer.unit}

Bill: ${customer.bill}

Paid: ${customer.paid}

Due: ${customer.due}

Status: ${customer.status}


-------------------------

`;



});





let file = new Blob(

[text],

{

type:"text/plain"

}

);



let link=document.createElement("a");



link.href=

URL.createObjectURL(file);



link.download=

"Jamila_Bhavan_Customer_List.txt";



link.click();



}






// ===============================
// AUTO BACKUP
// ===============================


function autoBackup(){



let backup = {


customers:customers,


paymentHistory:paymentHistory,


backupTime:new Date().toISOString()



};



localStorage.setItem(

"autoBackup",

JSON.stringify(backup)

);



}




// Auto backup after every save

setInterval(

autoBackup,

60000

);


// ======================================
// Jamila Bhavan
// Gas Meter Management System
// script.js Version 2.0
// Part - 8
// Final System Control
// ======================================



// ===============================
// AUTO DATE SET
// ===============================


function setTodayDate(){


let today = 
new Date()
.toISOString()
.split("T")[0];



let readingDate =

document.getElementById("readingDate");



let paymentDate =

document.getElementById("paymentDate");



if(readingDate){

readingDate.value = today;

}



if(paymentDate){

paymentDate.value = today;

}



}





// ===============================
// CLOSE MODAL BY OUTSIDE CLICK
// ===============================


window.onclick=function(e){



let entryModal =

document.getElementById("entryModal");



let paymentModal =

document.getElementById("paymentModal");



let reportModal =

document.getElementById("reportModal");




if(e.target === entryModal){


entryModal.style.display="none";


}



if(e.target === paymentModal){


paymentModal.style.display="none";


}



if(e.target === reportModal){


reportModal.style.display="none";


}



};






// ===============================
// ESC KEY CLOSE
// ===============================


document.addEventListener(

"keydown",

function(e){


if(e.key === "Escape"){



let modals =

document.querySelectorAll(".modal");



modals.forEach(modal=>{


modal.style.display="none";


});



}



}

);






// ===============================
// NUMBER FORMAT
// ===============================


function formatMoney(number){


return Number(number)

.toLocaleString(

"en-US",

{

minimumFractionDigits:2,

maximumFractionDigits:2

}

);


}







// ===============================
// FINAL REFRESH
// ===============================


function refreshSystem(){



renderTable();


updateDashboard();


setTodayDate();



console.log(

"Jamila Bhavan Gas Meter Management System Ready"

);



}







// ===============================
// START APPLICATION
// ===============================


document.addEventListener(

"DOMContentLoaded",

function(){



createDefaultCustomers();


loadFlats();


renderTable();


updateDashboard();


setTodayDate();



});







// ===============================
// APP INFORMATION
// ===============================


const APP_INFO={


name:"Jamila Bhavan Gas Meter Management System",


version:"2.0",


developer:"Rezaul Haque",


meterCount:28



};



console.log(APP_INFO);


// ==================================================
// START: WhatsApp Customer Due Message Feature
// এই অংশ নতুন যোগ করা হচ্ছে
// কাজ: Flat অনুযায়ী Customer Mobile Number নিয়ে
// WhatsApp-এ Bill/Due Message পাঠানো
// ==================================================


function sendCustomerWhatsApp(index){


    // Selected customer data নেওয়া
    let customer = customers[index];


    // Mobile number না থাকলে বন্ধ হবে
    if(!customer.mobile){

        alert("Mobile Number Not Found");

        return;

    }



    // WhatsApp Message তৈরি
   // ==============================
// ==============================
// WhatsApp Message Template Start
// ==============================

let message = `JAMILA BHAVAN
Gas Meter Bill

Dear ${customer.name || "Customer"},

Flat No     : ${customer.flat}
Meter No    : ${customer.meter}
Total Unit  : ${customer.unit}
Total Bill  : ${customer.bill.toFixed(2)} Tk
Paid Amount : ${customer.paid.toFixed(2)} Tk
Current Due : ${customer.due.toFixed(2)} Tk

Please pay your due amount at your earliest convenience.

Thank you.

Jamila Bhavan Management`;

// ============================
// WhatsApp Message Template End
// ============================



    // Mobile number পরিষ্কার করা
    let phone =
    customer.mobile.replace(/\D/g,'');



    // WhatsApp Link তৈরি
    let whatsappURL =

    "https://wa.me/88" +
    phone +
    "?text=" +
    encodeURIComponent(message);



    // WhatsApp Open
  // WhatsApp Open
window.open(whatsappURL, "_blank");


}


// ===============================
// MOBILE CUSTOMER DETAILS
// ===============================

document.querySelector("#customerTable tbody")
.addEventListener("click", function(e){

    if(window.innerWidth > 600){
        return;
    }

    let row = e.target.closest("tr");

    if(!row){
        return;
    }

    let index = Number(row.dataset.index);

    let oldDetails =
        row.nextElementSibling;

    if(
        oldDetails &&
        oldDetails.classList.contains("mobile-details")
    ){

        oldDetails.remove();

        return;
    }

    let customer = customers[index];

    let detailsRow =
        document.createElement("tr");

    detailsRow.className =
        "mobile-details";

    detailsRow.innerHTML = `

        <td colspan="6">

            <div class="mobile-detail-box">

                <div>Meter: ${customer.meter}</div>

                <div>Mobile: ${customer.mobile || "-"}</div>

                <div>Previous: ${customer.previous}</div>

                <div>Current: ${customer.current}</div>

                <div>Bill: ${Number(customer.bill || 0).toFixed(2)}</div>

                <div>Paid: ${Number(customer.paid || 0).toFixed(2)}</div>

                <div>Status: ${customer.status}</div>

            </div>

        </td>

    `;

    row.after(detailsRow);

});



// ===============================
// MOBILE UPDATE CUSTOMER
// ===============================

const updateCustomerBtn =
    document.getElementById("updateCustomerBtn");

if(updateCustomerBtn){

    updateCustomerBtn.onclick = function(){

        let flat = prompt(
            "Enter Flat Number (Example: A-2)"
        );

        if(!flat){
            return;
        }

        flat = flat.trim().toUpperCase();

        let index = customers.findIndex(
            c => c.flat.toUpperCase() === flat
        );

        if(index === -1){

            alert("Flat Not Found");

            return;
        }

        selectedIndex = index;

        let customer = customers[index];

        document.getElementById("flatNo").value =
            customer.flat;

        document.getElementById("meterNo").value =
            customer.meter;

        document.getElementById("customerName").value =
            customer.name || "";

        document.getElementById("mobileNumber").value =
            customer.mobile || "";

        document.getElementById("previousReading").value =
            customer.previous || 0;

        document.getElementById("currentReading").value =
            customer.current || 0;

        document.getElementById("gasRate").value =
            GAS_RATE;

        document.getElementById("serviceCharge").value =
            SERVICE_CHARGE;

        document.getElementById("totalAmount").value =
            customer.bill || 0;

        document.getElementById("receivedAmount").value =
            customer.paid || 0;

        document.getElementById("currentDue").value =
            customer.due || 0;

        document.getElementById("entryModal")
            .style.display = "flex";

    };

}



// ==================================================
// END: WhatsApp Customer Due Message Feature
// এই অংশের নিচে আর কিছু যোগ করার দরকার নেই
// ==================================================




// ==========================================
// BACKUP BUTTON
// ==========================================

document
    .getElementById("backupBtn")
    .addEventListener("click", function () {

        exportBackup();

        alert(
            "✅ GMS Backup Successfully Downloaded."
        );

    });


// ==========================================
// RESTORE BUTTON
// ==========================================

document
    .getElementById("restoreBtn")
    .addEventListener("click", function () {

        importBackup();

    });
// ==========================================
// RESTORE BUTTON FIX
// ==========================================

document
    .getElementById("restoreBtn")
    .onclick = function () {

        const fileInput =
            document.createElement("input");

        fileInput.type = "file";

        fileInput.accept =
            ".json,application/json";

        fileInput.onchange = function (event) {

            const file =
                event.target.files[0];

            if (!file) {
                return;
            }

            const reader =
                new FileReader();

            reader.onload = function () {

                try {

                    const backupData =
                        JSON.parse(
                            reader.result
                        );

                    if (
                        !backupData.customers ||
                        !backupData.paymentHistory
                    ) {

                        alert(
                            "❌ Invalid GMS backup file."
                        );

                        return;
                    }


                    customers =
                        backupData.customers;

                    paymentHistory =
                        backupData.paymentHistory;


                    localStorage.setItem(
                        "customers",
                        JSON.stringify(customers)
                    );

                    localStorage.setItem(
                        "paymentHistory",
                        JSON.stringify(paymentHistory)
                    );


                    renderTable();

                    updateDashboard();


                    alert(
                        "✅ GMS data restored successfully."
                    );

                }

                catch (error) {

                    alert(
                        "❌ Backup file could not be restored."
                    );

                    console.error(error);

                }

            };

            reader.readAsText(file);

        };

        fileInput.click();

    };
