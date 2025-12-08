package com.example.halostad.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halostad.AppModule
import com.example.halostad.data.model.User
import com.example.halostad.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Mengambil repository dari AppModule
    private val repository = AppModule.authRepository

    // --- STATE UNTUK HASIL LOGIN/REGISTER ---
    // StateFlow untuk memantau apakah proses sedang Loading, Sukses, atau Error
    private val _authState = MutableStateFlow<UiState<User>?>(null)
    val authState: StateFlow<UiState<User>?> = _authState

    // --- FUNGSI LOGIN ---
    fun login(email: String, pass: String) {
        // Validasi input sederhana
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = UiState.Error("Email dan Password tidak boleh kosong.")
            return
        }

        viewModelScope.launch {
            // Panggil repository.login dan kumpulkan hasilnya
            repository.login(email, pass).collect { state ->
                _authState.value = state
            }
        }
    }

    // --- FUNGSI REGISTER ---
    fun register(name: String, email: String, pass: String, role: String) {
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            _authState.value = UiState.Error("Semua kolom harus diisi.")
            return
        }

        viewModelScope.launch {
            repository.register(name, email, pass, role).collect { state ->
                _authState.value = state
            }
        }
    }

    // --- FUNGSI LOGIN GOOGLE ---
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            repository.loginWithGoogle(idToken).collect { state ->
                _authState.value = state
            }
        }
    }

    // Fungsi untuk mereset state (misal setelah error ditampilkan, kita reset jadi null)
    fun resetState() {
        _authState.value = null
    }
}