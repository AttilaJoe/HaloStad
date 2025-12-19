package com.example.halostad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String = "",
    val content: String = "",
    val category: String = "",
    val timestamp: Long = 0L,     // Kita simpan timestamp dalam Long (milliseconds)
    val answer: String = "",
    val ustadzId: String = "",
    val ustadzName: String = "",
    val isAnswered: Boolean = false,
    val answeredAt: Long = 0L     // Timestamp jawaban
)
