package com.example.halostad

import android.app.Application

class HaloStadApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.initialize(this)
    }
}
