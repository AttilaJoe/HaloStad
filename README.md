# HaloStad

<p align="center">
  <img src="docs/logo.png" width="180" alt="HaloStad Logo">
</p>

<p align="center">
A modern Islamic lifestyle Android application built using <b>Kotlin</b>, <b>Jetpack Compose</b>, <b>Firebase</b>, and <b>Room Database</b>.
</p>

<p align="center">

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Kotlin-2.x-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-blue)
![Firebase](https://img.shields.io/badge/Firebase-Enabled-orange)
![Database](https://img.shields.io/badge/Room-Database-success)
![License](https://img.shields.io/badge/License-MIT-blue)

</p>

---

# Table of Contents

- About
- Features
- Screenshots
- Technology Stack
- Application Architecture
- Project Structure
- Prerequisites
- Installation
- Firebase Configuration
- Running the Application
- Database
- Authentication
- Folder Explanation
- Dependency Overview
- Developer Guide
- Future Improvements
- Contributors
- License

---

# About

HaloStad merupakan aplikasi Android yang dikembangkan untuk membantu pengguna muslim dalam memperoleh informasi keislaman secara praktis melalui satu aplikasi.

Aplikasi menggabungkan beberapa layanan seperti:

- Jadwal waktu sholat berdasarkan lokasi pengguna
- Forum diskusi dan tanya jawab
- Profil pengguna
- Kamera
- Penyimpanan data lokal
- Login menggunakan Firebase Authentication
- Cloud Database menggunakan Firebase Firestore

Project ini dibangun menggunakan pendekatan **Modern Android Development** dengan Jetpack Compose sehingga seluruh antarmuka dibuat secara deklaratif.

---

# Main Features

## Authentication

- Login Email
- Register
- Forgot Password
- Firebase Authentication
- Session Management

---

## Prayer Time

- Mengambil lokasi pengguna
- Menghitung waktu sholat menggunakan Adhan Library
- Lokasi otomatis menggunakan GPS

---

## Discussion Feed

- Melihat postingan pengguna
- Membuat postingan baru
- Menampilkan daftar pertanyaan
- Penyimpanan Firestore

---

## Profile

- Melihat profil pengguna
- Edit profil
- Riwayat pertanyaan

---

## Camera

- Menggunakan CameraX
- Preview kamera
- Mengambil gambar

---

## Offline Storage

Menggunakan Room Database untuk menyimpan data lokal.

---

# Screenshots

Tambahkan screenshot aplikasi pada folder berikut.

```
docs/
    login.png
    register.png
    home.png
    feed.png
    profile.png
```

---

# Technology Stack

## Programming Language

- Kotlin

## UI

- Jetpack Compose
- Material 3

## Architecture

- MVVM

## Database

- Firebase Firestore
- Room Database

## Authentication

- Firebase Authentication

## Cloud

- Firebase

## Local Storage

- Room

## Dependency Injection

- AppModule

## Location

- Google Play Services Location

## Camera

- CameraX

## Image Loading

- Coil

## Navigation

- Navigation Compose

## Date Time

- Kotlinx Datetime

## Prayer Calculation

- Adhan Library

---

# Architecture

```
Presentation Layer
│
├── UI
├── ViewModel
│
Domain
│
└── Repository
│
Data Layer
│
├── Firebase Authentication
├── Firestore
├── Room Database
└── Local Entity
```

Project mengikuti pola **MVVM (Model View ViewModel)**.

---

# Project Structure

```
app
│
├── data
│   ├── local
│   │   ├── dao
│   │   ├── entity
│   │   └── AppDatabase
│   │
│   ├── model
│   │
│   └── repository
│
├── ui
│   ├── auth
│   ├── home
│   ├── navigation
│   ├── post
│   ├── profile
│   └── components
│
├── MainActivity.kt
├── HaloStadApp.kt
└── AppModule.kt
```

---

# Navigation

Aplikasi memiliki beberapa halaman utama:

- Login
- Register
- Forgot Password
- Home
- Feed
- Create Post
- Profile
- Edit Profile
- Riwayat Tanya
- Camera

---

# Authentication

Menggunakan Firebase Authentication.

Fitur:

- Register
- Login
- Logout
- Reset Password

---

# Firestore

Firestore digunakan untuk menyimpan:

- Data User
- Data Postingan
- Informasi Profil

---

# Room Database

Database lokal digunakan untuk penyimpanan offline.

Komponen:

- AppDatabase
- PostDao
- PostEntity

---

# Permissions

Aplikasi membutuhkan permission berikut.

```
INTERNET

ACCESS_FINE_LOCATION

ACCESS_COARSE_LOCATION

CAMERA

WRITE_EXTERNAL_STORAGE
```

---

# Requirements

- Android Studio Narwhal / terbaru
- JDK 11
- Gradle terbaru
- Android SDK 36
- Emulator Android atau perangkat fisik

---

# Installation

Clone repository

```bash
git clone https://github.com/AttilaJoe/HaloStad.git
```

Masuk ke project

```bash
cd HaloStad
```

Buka menggunakan Android Studio.

Sync Gradle.

Jalankan aplikasi.

---

# Firebase Configuration

Project menggunakan Firebase.

Pastikan file berikut tersedia.

```
google-services.json
```

Jika ingin menggunakan project Firebase sendiri:

1. Buat project Firebase
2. Tambahkan aplikasi Android
3. Download google-services.json
4. Letakkan pada

```
app/google-services.json
```

Aktifkan:

- Authentication
- Firestore
- Analytics

---

# Build

Debug

```
Build > Make Project
```

atau

```
Run App
```

Release

```
Build APK
```

atau

```
Generate Signed Bundle
```

---

# Dependency Overview

Library yang digunakan antara lain:

- Jetpack Compose
- Material3
- Navigation Compose
- Firebase Authentication
- Firebase Firestore
- Firebase Analytics
- Google Play Services Auth
- Google Play Services Location
- CameraX
- Coil
- Room
- Kotlinx Datetime
- Adhan Library
- Accompanist Permission

---

# Folder Explanation

## data

Seluruh model, repository, database, DAO dan entity.

---

## ui

Seluruh tampilan aplikasi.

Terdiri dari:

- Authentication
- Home
- Feed
- Profile
- Camera
- Navigation

---

## repository

Lapisan komunikasi antara ViewModel dengan sumber data.

---

## model

Representasi objek aplikasi.

Contoh:

- User
- Article
- Post

---

## local

Database lokal menggunakan Room.

---

# Developer Guide

Project menggunakan pola MVVM.

Flow data:

```
UI

↓

ViewModel

↓

Repository

↓

Firebase / Room

↓

UI
```

Developer disarankan menjaga pemisahan layer agar kode tetap mudah dipelihara.

---

# Coding Convention

- Gunakan Kotlin Style Guide
- Hindari business logic di Compose
- Simpan state pada ViewModel
- Repository hanya menangani data
- UI hanya menampilkan data

---

# Future Improvements

Beberapa fitur yang dapat dikembangkan:

- Dark Mode
- Push Notification
- Chat antar pengguna
- Bookmark artikel
- Upload gambar postingan
- Komentar
- Like postingan
- Admin Dashboard
- Multi-language
- Offline synchronization

---

# Contributors

Developed by:

**HaloStad Development Team**

Universitas Brawijaya

---

# License

Project ini dibuat untuk keperluan akademik dan pengembangan pembelajaran.

Silakan gunakan, modifikasi, dan kembangkan sesuai kebutuhan dengan tetap mencantumkan atribusi kepada pengembang.

---

# Acknowledgement

Terima kasih kepada:

- Kotlin Team
- Android Developers
- Jetpack Compose
- Firebase
- Google Play Services
- CameraX
- Room Database
- Adhan Library
- Coil
- Universitas Brawijaya

---

<p align="center">

Made with ❤️ using Kotlin & Jetpack Compose

</p>
