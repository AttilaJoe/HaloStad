package com.example.halostad.data.repository

import com.example.halostad.data.local.dao.PostDao
import com.example.halostad.data.local.entity.PostEntity
import com.example.halostad.data.model.Post
import com.example.halostad.utils.UiState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class PostRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val postDao: PostDao
) : PostRepository {

    override suspend fun createPost(post: Post): Flow<UiState<Boolean>> = callbackFlow {
        trySend(UiState.Loading)
        try {
            val docRef = firestore.collection("posts").document()
            val finalPost = post.copy(id = docRef.id)

            docRef.set(finalPost).await()
            trySend(UiState.Success(true))
        } catch (e: Exception) {
            trySend(UiState.Error(e.localizedMessage ?: "Gagal memposting"))
        }
        awaitClose { }
    }

    override fun getAllPosts(): Flow<UiState<List<Post>>> = callbackFlow {
        trySend(UiState.Loading)

        // 1. Emit Data Lokal Terlebih Dahulu (Cache)
        // Kita launch di coroutine terpisah agar tidak memblokir flow utama
        val localJob = launch(Dispatchers.IO) {
            try {
                val localPosts = postDao.getPosts().first()
                if (localPosts.isNotEmpty()) {
                    trySend(UiState.Success(localPosts.map { it.toDomain() }))
                }
            } catch (e: Exception) {
                // Abaikan error baca lokal
            }
        }

        // 2. Ambil Data Realtime dari Firestore
        val listener = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.localizedMessage ?: "Gagal memuat data"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val posts = snapshot.toObjects(Post::class.java)
                    
                    // Emit Data Terbaru ke UI
                    trySend(UiState.Success(posts))

                    // 3. Update Cache Lokal di Background
                    launch(Dispatchers.IO) {
                        postDao.deleteAllPosts() // Hapus cache lama
                        postDao.insertPosts(posts.map { it.toEntity() }) // Simpan cache baru
                    }
                }
            }

        awaitClose { 
            listener.remove() 
            localJob.cancel()
        }
    }

    override suspend fun answerPost(
        postId: String,
        answer: String,
        ustadzId: String,
        ustadzName: String
    ): Flow<UiState<Boolean>> = callbackFlow {
        trySend(UiState.Loading)
        try {
            val updates = mapOf(
                "answer" to answer,
                "ustadzId" to ustadzId,
                "ustadzName" to ustadzName,
                "isAnswered" to true,
                "answeredAt" to Timestamp.now()
            )

            firestore.collection("posts").document(postId).update(updates).await()
            trySend(UiState.Success(true))
        } catch (e: Exception) {
            trySend(UiState.Error(e.localizedMessage ?: "Gagal mengirim jawaban"))
        }
        awaitClose { }
    }

    // --- MAPPING FUNCTIONS ---

    private fun Post.toEntity(): PostEntity {
        return PostEntity(
            id = id,
            userId = userId,
            userName = userName,
            content = question, // Mapping question ke content di Entity
            category = category,
            timestamp = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000, // Timestamp to LongMillis
            isAnswered = isAnswered,
            answer = answer ?: "",
            ustadzId = ustadzId ?: "",
            ustadzName = ustadzName ?: "",
            answeredAt = answeredAt?.let { it.seconds * 1000 + it.nanoseconds / 1000000 } ?: 0L
        )
    }

    private fun PostEntity.toDomain(): Post {
        return Post(
            id = id,
            userId = userId,
            userName = userName,
            question = content, // Mapping content back to question
            category = category,
            timestamp = Timestamp(Date(timestamp)), // LongMillis to Timestamp
            isAnswered = isAnswered,
            answer = if (answer.isNotEmpty()) answer else null,
            ustadzId = if (ustadzId.isNotEmpty()) ustadzId else null,
            ustadzName = if (ustadzName.isNotEmpty()) ustadzName else null,
            answeredAt = if (answeredAt != 0L) Timestamp(Date(answeredAt)) else null
        )
    }
}