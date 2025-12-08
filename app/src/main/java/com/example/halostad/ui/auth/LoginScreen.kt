package com.example.halostad.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.navigation.NavController
import com.example.halostad.R
import com.example.halostad.ui.navigation.Screen
import com.example.halostad.utils.UiState

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by viewModel.authState.collectAsState()

    val isFormValid = email.isNotBlank() && password.isNotBlank()

    // warna-warna utama
    val accentGreen = Color(0xFF2D8A5B)     // hijau utama
    val goldYellow = Color(0xFFF4B000)      // kuning gold untuk "Stad"
    // === LAUNCHER GOOGLE SIGN IN ===
    // 1. Konfigurasi Google Sign In
    // Pastikan "default_web_client_id" ada (otomatis dari google-services.json)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // 2. Launcher untuk menangani hasil dari Intent Google Sign In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                // 3. Kirim ID Token ke ViewModel untuk login ke Firebase
                viewModel.loginWithGoogle(idToken)
            } else {
                Toast.makeText(context, "Google Sign-In gagal: ID Token null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In error: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    val disabledGrey = Color(0xFFBDBDBD)

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                Toast.makeText(
                    context,
                    "Login Berhasil! Selamat datang ${state.data.name}",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
                viewModel.resetState()
            }

            is UiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }

            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // === LOGO ===
            Image(
                painter = painterResource(id = R.drawable.ic_hirup_logo),
                contentDescription = "Logo HaloStad",
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // === Teks "HaloStad" (Hijau + Gold) ===
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = accentGreen)) {
                        append("Halo")
                    }
                    withStyle(SpanStyle(color = goldYellow)) {
                        append("Stad")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // === Jargon aplikasi (tanya ustadz) ===
            Text(
                text = "Tempat bertanya seputar Islam langsung ke ustadz terpercaya.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // ==== FORM LOGIN ====
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // EMAIL
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = accentGreen,
                        cursorColor = accentGreen,

                        unfocusedLeadingIconColor = Color.Black,
                        focusedLeadingIconColor = accentGreen,

                        unfocusedPlaceholderColor = Color.Gray,
                        focusedPlaceholderColor = accentGreen,

                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PASSWORD
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Kata sandi") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = accentGreen,
                        cursorColor = accentGreen,

                        unfocusedLeadingIconColor = Color.Black,
                        focusedLeadingIconColor = accentGreen,
                        unfocusedTrailingIconColor = Color.Black,
                        focusedTrailingIconColor = accentGreen,

                        unfocusedPlaceholderColor = Color.Gray,
                        focusedPlaceholderColor = accentGreen,

                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Lupa sandi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    TextButton(
                        onClick = { /* TODO: aksi lupa sandi */ },
                        modifier = Modifier.align(Alignment.CenterEnd),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Lupa sandi?",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // === TOMBOL MASUK ===
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    enabled = isFormValid && uiState !is UiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentGreen,
                        contentColor = Color.White,
                        disabledContainerColor = disabledGrey,
                        disabledContentColor = Color.White
                    )
                ) {
                    if (uiState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Masuk",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === PEMBATAS "Atau" ===
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.LightGray
                    )
                    Text(
                        text = "  Atau  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // === TOMBOL GOOGLE ===
                OutlinedButton(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Masuk dengan Google",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // === FOOTER: Belum punya akun? Daftar ===
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Belum punya akun? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    TextButton(
                        onClick = { navController.navigate(Screen.Register.route) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Daftar",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
