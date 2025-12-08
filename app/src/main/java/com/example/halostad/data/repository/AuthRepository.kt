package com.example.halostad.data.repository

import com.example.halostad.data.model.User
import com.example.halostad.utils.UiState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Fungsi login mengembalikan Flow UiState berisi data User
    suspend fun login(email: String, pass: String): Flow<UiState<User>>

    // Fungsi register juga mengembalikan Flow UiState berisi data User yang baru dibuat
    suspend fun register(name: String, email: String, pass: String, role: String): Flow<UiState<User>>

    // Google Sign In
    suspend fun loginWithGoogle(idToken: String): Flow<UiState<User>>

    // Reset Password
    suspend fun resetPassword(email: String): Flow<UiState<Boolean>>

    fun logout()

    // Cek apakah user sedang login (untuk auto-login saat buka aplikasi)
    fun getCurrentUser(): com.google.firebase.auth.FirebaseUser?

    // Ubah baris updateProfile menjadi:
    suspend fun updateProfile(name: String, photoBase64: String?): Flow<UiState<Boolean>>
}