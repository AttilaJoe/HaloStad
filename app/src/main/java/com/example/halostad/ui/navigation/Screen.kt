package com.example.halostad.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")     // Page 1: Waktu Sholat
    object Feed : Screen("feed")     // Page 2: Tanya Jawab
    object Profile : Screen("profile") // Page 3: Profil
    object CreatePost : Screen("create_post")
    object EditProfile : Screen("edit_profile")
    object RiwayatTanya : Screen("riwayat_tanya")
    object Camera : Screen("camera")
    object ForgotPassword : Screen("forgot_password")
}