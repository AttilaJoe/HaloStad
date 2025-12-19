package com.example.halostad

import android.content.Context
import androidx.room.Room
import com.example.halostad.data.local.AppDatabase
import com.example.halostad.data.repository.AuthRepository
import com.example.halostad.data.repository.AuthRepositoryImpl
import com.example.halostad.data.repository.PostRepository
import com.example.halostad.data.repository.PostRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AppModule {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Database harus di-init dengan Context
    lateinit var appDatabase: AppDatabase
        private set

    fun initialize(context: Context) {
        appDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "halostad_db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(auth, firestore)
    }

    val postRepository: PostRepository by lazy {
        // Pastikan initialize() sudah dipanggil di Application Class sebelum akses ini
        PostRepositoryImpl(firestore, appDatabase.postDao())
    }
}