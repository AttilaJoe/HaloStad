package com.example.halostad.ui.profile

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halostad.AppModule
import com.example.halostad.ui.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavController) {
    val currentUser = AppModule.authRepository.getCurrentUser()

    var role by remember { mutableStateOf("Loading...") }
    var profileImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Notifikasi Adzan (dummy)
    var adzanNotif by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

        if (currentUser != null) {
            listenerRegistration = FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
                .addSnapshotListener { doc, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    if (doc != null && doc.exists()) {
                        role = doc.getString("role")?.replaceFirstChar { it.uppercase() } ?: "User"

                        val base64String = doc.getString("photoBase64")
                        if (!base64String.isNullOrBlank()) {
                            try {
                                val cleanBase64 = base64String.substringAfter(",")
                                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                profileImageBitmap = bmp.asImageBitmap()
                            } catch (_: Exception) {
                            }
                        }
                    }
                    isLoading = false
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // ikut dark mode
    ) {

        // ================= HEADER =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF2D8A5B), Color(0xFF16A34A))
                    ),
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Judul naik sedikit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)  // **GESER KE ATAS**
                ) {
                    Text(
                        text = "Profil Saya",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = {
                        navController.navigate(Screen.EditProfile.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Foto Profil
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .offset(y = (-24

                                 ).dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        profileImageBitmap != null -> {
                            androidx.compose.foundation.Image(
                                bitmap = profileImageBitmap!!,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            AsyncImage(
                                model = "https://ui-avatars.com/api/?name=${currentUser?.displayName ?: "User"}",
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser?.displayName ?: "Nama Pengguna",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Assalamu'alaikum, ${currentUser?.displayName?.split(' ')?.firstOrNull() ?: "User"}",
                    color = Color(0xFFE5F5EC),
                    fontSize = 14.sp
                )
            }
        }

        // ================= CARD + LOGOUT =================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 210.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(vertical = 20.dp)) {

                    ProfileToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifikasi Adzan",
                        subtitle = "Pengingat waktu sholat",
                        checked = adzanNotif,
                        onCheckedChange = { adzanNotif = it },
                        iconBackground = Color(0xFFE8F5E9)
                    )

                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))

                    ProfileArrowItem(
                        icon = Icons.Default.Bookmark,
                        title = "Artikel Tersimpan",
                        subtitle = "24 artikel",
                        onClick = {},
                        iconBackground = Color(0xFFE8F5E9)
                    )

                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))

                    ProfileArrowItem(
                        icon = Icons.Default.Help,
                        title = "Riwayat Tanya Ustad",
                        subtitle = "12 pertanyaan",
                        onClick = {
                            navController.navigate(Screen.RiwayatTanya.route)
                        },
                        iconBackground = Color(0xFFE8F5E9)
                    )

                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))

                    ProfileArrowItem(
                        icon = Icons.Default.Chat,
                        title = "Riwayat Chat AI",
                        subtitle = "8 percakapan",
                        onClick = {},
                        iconBackground = Color(0xFFE8F5E9)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    AppModule.authRepository.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, Color(0xFFE53935)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFE53935))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar dari Akun", color = Color(0xFFE53935), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconBackground: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ProfileArrowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconBackground: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
