package com.example.halostad.ui.profile

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory // Import Baru
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64 // Import Baru
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.halostad.AppModule
import com.example.halostad.utils.ImageHelper
import com.example.halostad.utils.UiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.firestore.FirebaseFirestore // Import Baru

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser = AppModule.authRepository.getCurrentUser()

    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }

    // Bitmap untuk gambar BARU yang dipilih user (Galeri/Kamera)
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Bitmap untuk gambar LAMA dari Database (Firestore)
    var currentImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val updateState by viewModel.updateState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // --- LOAD DATA DARI FIRESTORE (BARU) ---
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
                .get().addOnSuccessListener { document ->
                    // 1. Sinkronisasi Nama terbaru
                    val dbName = document.getString("name")
                    if (!dbName.isNullOrEmpty()) {
                        name = dbName
                    }

                    // 2. Sinkronisasi Foto Base64
                    val base64String = document.getString("photoBase64")
                    if (!base64String.isNullOrBlank()) {
                        try {
                            val cleanBase64 = base64String.substringAfter(",")
                            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                            val bitmap =
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            currentImageBitmap = bitmap
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        }
    }

    // --- LAUNCHER GALERI ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                selectedBitmap = bitmap
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- DETEKSI HASIL KAMERA DARI CUSTOM CAMERA SCREEN ---
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    
    DisposableEffect(savedStateHandle) {
        val liveData = savedStateHandle?.getLiveData<String>("camera_result")
        val observer = androidx.lifecycle.Observer<String> { uriString ->
            if (uriString != null) {
                try {
                    val uri = Uri.parse(uriString)
                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } else {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    }
                    selectedBitmap = bitmap
                    
                    // Reset agar tidak terpanggil ganda
                    savedStateHandle?.remove<String>("camera_result")
                    
                    Toast.makeText(context, "Foto berhasil dimuat", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Gagal memuat hasil kamera: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe menggunakan Lifecycle owner dari screen ini
        liveData?.observe(navController.currentBackStackEntry!!, observer)

        onDispose {
            liveData?.removeObserver(observer)
        }
    }

    // Handle Sukses Update
    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) {
            Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            navController.popBackStack()
        } else if (updateState is UiState.Error) {
            Toast.makeText(context, (updateState as UiState.Error).message, Toast.LENGTH_LONG)
                .show()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF2D8A5B), Color(0xFF16A34A))
                    ),
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    "Edit Profil",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // FOTO PROFIL YANG SAMA DENGAN PROFILESCREEN
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable {
                            // TODO: buka image picker
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedBitmap != null) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (currentImageBitmap != null) {
                        Image(
                            bitmap = currentImageBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = "https://ui-avatars.com/api/?name=${name}",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ======================================================
        // ==================== FORM TEXT ========================
        // ======================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                Text("Galeri")
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        navController.navigate("camera") // Pindah ke Screen.Camera
                    } else {
                        if (cameraPermissionState.status.shouldShowRationale) {
                            Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_LONG).show()
                        }
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
            ) {
                Text("Kamera")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {

            Column(modifier = Modifier.padding(20.dp)) {

                Text("Nama", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    },
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // ======================================================
        // ==============  BUTTON SIMPAN (HIJAU)  ================
        // ======================================================

        Button(
            onClick = {
                // Hanya kirim base64Image jika user memilih gambar BARU (selectedBitmap tidak null)
                // Jika null, kirim null ke ViewModel (Repository akan abaikan update foto)
                val base64Image = selectedBitmap?.let { ImageHelper.bitmapToBase64(it) }
                viewModel.updateProfile(name, base64Image)
            },
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2D8A5B),
                contentColor = Color.White
            )
        ) {
            if (updateState is UiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Simpan Perubahan")
            }
        }
    }
}
