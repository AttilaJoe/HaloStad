package com.example.halostad.data.repository

import com.example.halostad.data.model.User
import com.example.halostad.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    // --- LOGIKA LOGIN ---
    override suspend fun login(email: String, pass: String): Flow<UiState<User>> = callbackFlow {
        try {
            trySend(UiState.Loading) // 1. Kirim status Loading

            // 2. Proses Login ke Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid

            if (uid != null) {
                // 3. Jika login auth sukses, AMBIL data detail user (role, nama, dll) dari Firestore
                val documentSnapshot = firestore.collection("users").document(uid).get().await()

                // Konversi dokumen Firestore menjadi objek User kita
                val user = documentSnapshot.toObject(User::class.java)

                if (user != null) {
                    trySend(UiState.Success(user)) // 4. Kirim Sukses beserta datanya
                } else {
                    trySend(UiState.Error("Data pengguna tidak ditemukan di database."))
                }
            } else {
                trySend(UiState.Error("Gagal mendapatkan ID Pengguna."))
            }

        } catch (e: Exception) {
            // Tangkap error (misal password salah, sinyal jelek)
            trySend(UiState.Error(e.localizedMessage ?: "Terjadi kesalahan tidak diketahui"))
        }
        awaitClose { }
    }

    // --- LOGIKA REGISTER ---
    override suspend fun register(name: String, email: String, pass: String, role: String): Flow<UiState<User>> = callbackFlow {
        try {
            trySend(UiState.Loading)

            // 1. Buat akun di Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid

            if (uid != null) {
                // 2. Siapkan data User untuk disimpan
                val newUser = User(
                    id = uid,
                    name = name,
                    email = email,
                    role = role, // 'user' atau 'ustadz'
                    photoUrl = "",
                    gender = "" // Bisa diupdate nanti di profil
                )

                // 3. Simpan data tersebut ke Firestore di koleksi 'users'
                firestore.collection("users").document(uid).set(newUser).await()

                // 4. Kirim Sukses
                trySend(UiState.Success(newUser))
            } else {
                trySend(UiState.Error("Gagal membuat User ID."))
            }

        } catch (e: Exception) {
            trySend(UiState.Error(e.localizedMessage ?: "Gagal mendaftar"))
        }
        awaitClose { }
    }

    override suspend fun loginWithGoogle(idToken: String): Flow<UiState<User>> = callbackFlow {
        try {
            trySend(UiState.Loading)

            // 1. Buat credential dari ID Token
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            
            // 2. Sign In ke Firebase
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            val uid = firebaseUser?.uid

            if (uid != null) {
                // 3. Cek apakah dokumen user sudah ada di Firestore
                val docRef = firestore.collection("users").document(uid)
                val docSnapshot = docRef.get().await()

                if (docSnapshot.exists()) {
                    // User sudah terdaftar, ambil datanya
                    val existingUser = docSnapshot.toObject(User::class.java)
                    if (existingUser != null) {
                        trySend(UiState.Success(existingUser))
                    } else {
                        trySend(UiState.Error("Gagal mengambil data user."))
                    }
                } else {
                    // User baru (belum ada di Firestore), buatkan dokumen baru
                    val newUser = User(
                        id = uid,
                        name = firebaseUser.displayName ?: "No Name",
                        email = firebaseUser.email ?: "",
                        role = "user", // Default role
                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                        gender = ""
                    )
                    docRef.set(newUser).await()
                    trySend(UiState.Success(newUser))
                }
            } else {
                trySend(UiState.Error("Google Sign-In gagal: UID tidak ditemukan"))
            }

        } catch (e: Exception) {
            trySend(UiState.Error(e.localizedMessage ?: "Login Google Gagal"))
        }
        awaitClose { }
    }

    // --- RESET PASSWORD ---
    override suspend fun resetPassword(email: String): Flow<UiState<Boolean>> = callbackFlow {
        try {
            trySend(UiState.Loading)

            // 1. Cek dulu di Firestore apakah email terdaftar
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // 2. Jika ada, kirim email reset password
                auth.sendPasswordResetEmail(email).await()
                trySend(UiState.Success(true))
            } else {
                // 3. Jika tidak ada
                trySend(UiState.Error("Email tidak ditemukan. Silakan daftar terlebih dahulu."))
            }

        } catch (e: Exception) {
            trySend(UiState.Error(e.localizedMessage ?: "Gagal mengirim email reset"))
        }
        awaitClose { }
    }

    override suspend fun updateProfile(name: String, photoBase64: String?): Flow<UiState<Boolean>> = callbackFlow {
        trySend(UiState.Loading)
        try {
            val user = auth.currentUser
            if (user == null) {
                trySend(UiState.Error("User tidak ditemukan"))
                close()
                return@callbackFlow
            }

            // 1. Update Nama di Firebase Auth (Agar sesi login terupdate)
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                // Kita TIDAK update PhotoURL di Auth karena Auth tidak support Base64 panjang
                .build()
            user.updateProfile(profileUpdates).await()

            // 2. Update Nama & Foto di Firestore (Database)
            val updates = mutableMapOf<String, Any>(
                "name" to name
            )

            // Hanya update foto jika ada foto baru yang dikirim
            if (photoBase64 != null) {
                updates["photoBase64"] = photoBase64
            }

            firestore.collection("users").document(user.uid).update(updates).await()

            trySend(UiState.Success(true))

        } catch (e: Exception) {
            trySend(UiState.Error(e.localizedMessage ?: "Gagal update profil"))
        }
        awaitClose { }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUser() = auth.currentUser
}