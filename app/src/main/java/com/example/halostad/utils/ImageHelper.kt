package com.example.halostad.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageHelper {

    // Fungsi Mengubah Bitmap -> String Base64 (Dikpresi)
    fun bitmapToBase64(bitmap: Bitmap): String {
        // 1. Resize gambar jika terlalu besar (Maksimal lebar/tinggi 600px)
        val resizedBitmap = resizeBitmap(bitmap, 600)

        val outputStream = ByteArrayOutputStream()

        // 2. Kompres ke JPEG kualitas 60% (Agar size di bawah 500KB aman untuk Firestore)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)

        val byteArrays = outputStream.toByteArray()

        // 3. Ubah ke String Base64
        val base64String = Base64.encodeToString(byteArrays, Base64.NO_WRAP)

        // Tambahkan prefix agar bisa dibaca library gambar (Coil)
        return "data:image/jpeg;base64,$base64String"
    }

    // Fungsi Bantuan: Resize Gambar
    private fun resizeBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }
}