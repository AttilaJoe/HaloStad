package com.example.halostad.ui.home

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.halostad.AppModule
import com.example.halostad.R
import com.example.halostad.utils.PrayerTimeHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.ExperimentalFoundationApi
import android.content.IntentSender
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    val jadwalSholat by viewModel.jadwalSholat.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser = AppModule.authRepository.getCurrentUser()

    val displayName = currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")
        ?: "User"
    val firstName = displayName.substringBefore(" ")

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Launcher untuk menangani hasil dialog "Turn on Location"
    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // User menekan "OK", lokasi aktif -> Ambil data
            viewModel.getUserLocationAndPrayerTimes(context)
        } else {
            // User menekan "No Thanks" atau batal
            // Opsional: Tampilkan pesan manual atau biarkan tombol refresh yang bekerja
        }
    }

    // Fungsi untuk Cek apakah GPS Nyala
    fun checkLocationSetting(
        onEnabled: () -> Unit,
        onDisabled: (IntentSenderRequest) -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onEnabled()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    onDisabled(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore
                }
            }
        }
    }

    // Ambil lokasi & jadwal saat izin sudah dikasih DAN GPS nyala
    LaunchedEffect(key1 = locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            checkLocationSetting(
                onEnabled = {
                    viewModel.getUserLocationAndPrayerTimes(context)
                },
                onDisabled = { intentSenderRequest ->
                    settingResultRequest.launch(intentSenderRequest)
                }
            )
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            // ===== HEADER =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Assalamualaikum, $firstName",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.padding(top = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Lokasi",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = jadwalSholat?.lokasi ?: "Lokasi belum tersedia",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // jam & tanggal sengaja DIHAPUS
                }

                IconButton(
                    onClick = {
                        if (locationPermissionsState.allPermissionsGranted) {
                            checkLocationSetting(
                                onEnabled = {
                                    viewModel.getUserLocationAndPrayerTimes(context)
                                },
                                onDisabled = { intentSenderRequest ->
                                    settingResultRequest.launch(intentSenderRequest)
                                }
                            )
                        } else {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Lokasi"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== JUDUL PRAYER TIME DI LUAR KARTU =====
            Text(
                text = "Prayer time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== KARTU SHOLAT (KARUSEL) =====
            if (locationPermissionsState.allPermissionsGranted) {
                if (jadwalSholat != null) {
                    PrayerCarouselCard(
                        jadwal = jadwalSholat!!
                    )
                } else if (!isLoading) {
                    Text("Tekan tombol refresh untuk memuat data.")
                }
            } else {
                Text("Izin lokasi diperlukan.")
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Berikan Izin")
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== TRACKER: SUDAH SHOLAT {X} HARI INI? =====
            jadwalSholat?.let { jadwal ->
                PrayerTrackerCard(jadwal = jadwal)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== BOX BARU: ARTIKEL TERKAIT =====
            RelatedArticlesSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ===========================================================
//  KARUSEL JADWAL SHOLAT
// ===========================================================

data class PrayerCarouselItem(
    val name: String,
    val time: LocalTime,
    val imageRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrayerCarouselCard(
    jadwal: PrayerTimeHelper.JadwalSholat
) {
    val locale = Locale("id", "ID")
    val today = LocalDate.now()
    val dayName = today.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val dateStr = today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
    val now = LocalTime.now()

    val prayers = remember(jadwal) {
        listOfNotNull(
            "Subuh" to jadwal.subuh,
            "Dzuhur" to jadwal.dzuhur,
            "Ashar" to jadwal.ashar,
            "Maghrib" to jadwal.maghrib,
            "Isya" to jadwal.isya
        ).mapNotNull { (name, timeStr) ->
            try {
                PrayerCarouselItem(
                    name = name,
                    time = LocalTime.parse(timeStr),
                    imageRes = when (name) {
                        "Subuh" -> R.drawable.bg_subuh
                        "Dzuhur" -> R.drawable.bg_dzuhur
                        "Ashar" -> R.drawable.bg_ashar
                        "Maghrib" -> R.drawable.bg_maghrib
                        else -> R.drawable.bg_isya
                    }
                )
            } catch (_: Exception) {
                null
            }
        }.sortedBy { it.time }
    }

    // page awal = current / next sholat
    val initialPage = remember(prayers, now) {
        val (cur, next) = getCurrentAndNextPrayer(now, prayers.map { it.name to it.time })
        val target = cur?.first ?: next?.first
        prayers.indexOfFirst { it.name == target }.coerceAtLeast(0)
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { prayers.size }
    )

    // AUTO SCROLL
    LaunchedEffect(pagerState.currentPage, prayers.size) {
        if (prayers.isNotEmpty()) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % prayers.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            // SLIDE UTAMA – gambar mepet ke tepi kartu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val item = prayers[page]

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // overlay HIJAU
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xAA16A34A),
                                            Color(0xAA22C55E)
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = dayName,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = dateStr,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // ====== TEKS JAM BESAR ======
                                Text(
                                    text = "${item.name} ${
                                        item.time.format(DateTimeFormatter.ofPattern("HH:mm"))
                                    }",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                val diffMinutes =
                                    java.time.Duration.between(now, item.time).toMinutes()
                                if (diffMinutes > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Dalam ${diffMinutes} menit",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ROW WAKTU SHOLAT DI BAWAH
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                prayers.forEachIndexed { index, item ->
                    val isSelected = index == pagerState.currentPage
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.name,
                            fontSize = 12.sp,
                            color = if (isSelected) Color(0xFFEF4444)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Text(
                            text = item.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                            fontSize = 12.sp,
                            color = if (isSelected) Color(0xFFEF4444)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = Color(0xFFEF4444),
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

// ===========================================================
//  PERSONAL TRACKER
// ===========================================================

@Composable
fun PrayerTrackerCard(
    jadwal: PrayerTimeHelper.JadwalSholat
) {
    val now = LocalTime.now()

    val prayers = remember(jadwal) {
        listOfNotNull(
            "Subuh" to jadwal.subuh,
            "Dzuhur" to jadwal.dzuhur,
            "Ashar" to jadwal.ashar,
            "Maghrib" to jadwal.maghrib,
            "Isya" to jadwal.isya
        ).mapNotNull { (name, timeStr) ->
            try {
                name to LocalTime.parse(timeStr)
            } catch (_: Exception) {
                null
            }
        }.sortedBy { it.second }
    }

    val currentPrayer = remember(prayers, now) {
        getCurrentAndNextPrayer(now, prayers).first
    }

    var hasPrayed by remember { mutableStateOf<Boolean?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Personal Tracker",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sudah sholat ${currentPrayer?.first ?: "hari ini"}?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Gunakan tracker ini untuk mengingatkan diri sendiri agar tidak menunda sholat.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { hasPrayed = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF16A34A)
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF22C55E), Color(0xFF16A34A))
                        )
                    )
                ) {
                    Text("Belum")
                }

                Button(
                    onClick = { hasPrayed = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sudah")
                }
            }

            hasPrayed?.let { status ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (status)
                        "Alhamdulillah 🌙"
                    else
                        "Yuk jangan ditunda lagi 🙂",
                    fontSize = 12.sp,
                    color = if (status) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ===========================================================
//  BOX BARU: ARTIKEL TERKAIT
// ===========================================================

@Composable
fun RelatedArticlesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Artikel terkait",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Belum ada artikel yang ditampilkan.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

// ===========================================================
//  HELPER
// ===========================================================

private fun getCurrentAndNextPrayer(
    now: LocalTime,
    prayers: List<Pair<String, LocalTime>>
): Pair<Pair<String, LocalTime>?, Pair<String, LocalTime>?> {
    if (prayers.isEmpty()) return null to null

    var current: Pair<String, LocalTime>? = null
    var next: Pair<String, LocalTime>? = null

    for (i in prayers.indices) {
        val (name, time) = prayers[i]
        if (now.isBefore(time)) {
            next = name to time
            current = if (i == 0) null else prayers[i - 1]
            return current to next
        }
    }
    current = prayers.last()
    return current to null
}
