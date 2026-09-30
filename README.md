# Jamila Bhavan - Gas Meter Management System (Android)

Native Android application built with Kotlin and Jetpack Compose for managing gas meters, reading entries, automated billing calculations, payment records, monthly reports, and WhatsApp billing notices for **Jamila Bhavan-1** (Model City, Mouchak, Kaliakair, Gazipur).

---

## 📱 How to Install (কিভাবে ইন্সটল করবেন)

### ১. মোবাইল ফোনে সরাসরি APK ইন্সটল করার নিয়ম (Direct APK Installation):
1. **APK ডাউনলোড করুন:** AI Studio এর উপরের ডানদিকের মেনু (Settings / Download) থেকে **"Generate APK"** বা **"Download APK"** নির্বাচন করে `.apk` ফাইলটি ডাউনলোড করুন।
2. **ফোনে নিন:** ডাউনলোড করা APK ফাইলটি আপনার অ্যান্ড্রয়েড মোবাইল ফোনে পাঠান (USB, Google Drive বা WhatsApp এর মাধ্যমে)।
3. **অনুমতি দিন:** ফোনের File Manager থেকে APK ফাইলে ট্যাপ করুন। "Install Unknown Apps" বা "Allow from this source" পারমিশন চালু করুন।
4. **ইন্সটল করুন:** **"Install"** বাটনে চাপুন। কয়েক সেকেন্ডের মধ্যে আপনার ফোনে Jamila Bhavan Gas Meter অ্যাপটি ইনস্টল হয়ে যাবে।

### ২. ব্রাউজারেই সরাসরি লাইভ ব্যবহার (Streaming Android Emulator):
- Google AI Studio-এর স্ক্রিনের ডানপাশে লাইভ অ্যান্ড্রয়েড এমুলেটরে সরাসরি অ্যাপটি যেকোনো সময় চালাতে ও ব্যবহার করতে পারেন। কোনো আলাদা ইনস্টলেশনের প্রয়োজন নেই।

### ৩. Android Studio এর মাধ্যমে (Developers / Source Code):
1. AI Studio থেকে প্রজেক্টটি ZIP আকারে ডাউনলোড করুন।
2. ZIP ফাইলটি Extract করুন এবং Android Studio দিয়ে ফোল্ডারটি ওপেন করুন।
3. Gradle Sync শেষ হলে আপনার ফোন USB দিয়ে কানেক্ট করে অথবা Emulator সিলেক্ট করে **Run (Shift + F10)** চাপুন।
4. অথবা মেনু থেকে **Build > Build Bundle(s) / APK(s) > Build APK(s)** দিয়ে সরাসরি APK তৈরি করে নিন।

---

## 🔒 Data Safety & Integrity (ডেটা সুরক্ষার নিশ্চয়তা)

- **28 Flats অক্ষত রাখা হয়েছে:** ফ্ল্যাট A-2 থেকে D-8 পর্যন্ত প্রতিটি ফ্ল্যাট এবং মিটার নম্বর স্থায়ীভাবে সুরক্ষিত।
- **New Entry vs Update পৃথকীকরণ:**
  - **New Entry:** নতুন মাসের রিডিং এন্ট্রির সময় পূর্বের Current Reading স্বয়ংক্রিয়ভাবে Previous Reading হিসেবে বসে এবং নতুন Current Reading খালি থাকে। পূর্বের বাকি (Due) স্বয়ংক্রিয়ভাবে Previous Due হিসেবে যুক্ত হয়।
  - **Update:** বিদ্যমান রিডিং বা গ্রাহকের নাম/মোবাইল কোনো রেকর্ড না মুছেই সরাসরি সংশোধন করা যায়।
- **Never Reset / Wipe:** গ্রাহকের নাম, মোবাইল, পেমেন্ট হিস্ট্রি বা ফ্ল্যাটের তথ্য নষ্ট হওয়ার কোনো অপশন বা বাটন নেই।
- **Room Database Offline Persistence:** সম্পূর্ণ তথ্য ডিভাইসের লোকাল SQLite/Room ডাটাবেজে সম্পূর্ণ নিরাপদে সংরক্ষিত থাকে।
- **JSON Backup & Share:** যেকোনো সময় এক ক্লিকে সম্পূর্ণ গ্রাহক তথ্য ও পেমেন্ট হিস্ট্রি JSON ব্যাকআপ হিসেবে কপি বা শেয়ার করা যায়।

---

## 🌟 Features (মূল সুবিধাসমূহ)

- **28 Fixed Flats & Meters:** Complete database initialized with pre-assigned gas meters for A-2 to D-8.
- **Automated Gas Billing:** Computes units consumed (`current - previous`), total bill (`units * rate + service charge + previous due + late fee - discount`), and outstanding dues.
- **Payment Collection & History:** Record payments by Cash, Bank, or Mobile Banking with automatic due updates and transaction tracking.
- **WhatsApp Notification Integration:** Generates and dispatches individualized gas meter bill notices to tenant WhatsApp/mobile numbers.
- **Monthly Reports & Print/PDF:** Real-time billing summary with total units, bills, collections, dues, and Android print/PDF support.
- **Backup & Restore:** Full JSON export and import capabilities for complete tenant records and payment history.
- **Material 3 Design:** Responsive mobile UI adhering to Material Design 3 guidelines.
