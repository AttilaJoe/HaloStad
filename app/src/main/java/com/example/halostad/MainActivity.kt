package com.example.halostad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.halostad.ui.auth.LoginScreen
import com.example.halostad.ui.auth.RegisterScreen
import com.example.halostad.ui.home.HomeScreen
import com.example.halostad.ui.navigation.Screen
import com.example.halostad.ui.post.CreatePostScreen
import com.example.halostad.ui.post.FeedScreen
import com.example.halostad.ui.profile.ProfileScreen
import com.example.halostad.ui.profile.RiwayatTanyaScreen
import com.example.halostad.ui.theme.HaloStadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HaloStadTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    // Cek user login
    val currentUser = AppModule.authRepository.getCurrentUser()
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    // Daftar Item Bottom Bar
    val items = listOf(
        BottomNavItem("Beranda", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Tanya Jawab", Screen.Feed.route, Icons.Default.List),
        BottomNavItem("Profil", Screen.Profile.route, Icons.Default.Person)
    )

    // Logic: Sembunyikan BottomBar di halaman Login, Register, dan CreatePost
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = items.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up ke start destination agar tidak menumpuk stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Hindari duplikasi halaman jika diklik berulang
                                    launchSingleTop = true
                                    // Restore state (misal posisi scroll)
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.Register.route) { RegisterScreen(navController) }
            composable(Screen.ForgotPassword.route) { 
                com.example.halostad.ui.auth.ForgotPasswordScreen(navController) 
            }

            // Halaman Utama (Ada Bottom Bar)
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Feed.route) { FeedScreen(navController) }
            composable(Screen.Profile.route) { ProfileScreen(navController) }


            // Halaman Lain (Tanpa Bottom Bar)
            composable(Screen.CreatePost.route) { CreatePostScreen(navController) }

            // ... di dalam NavHost ...
            composable(Screen.EditProfile.route) {
                com.example.halostad.ui.profile.EditProfileScreen(navController)
            }
            composable(Screen.RiwayatTanya.route) {
                RiwayatTanyaScreen(navController)
            }
            composable(Screen.Camera.route) {
                com.example.halostad.ui.components.CameraScreen(navController)
            }
        }
    }
}

// Data class sederhana untuk item navigasi
data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)