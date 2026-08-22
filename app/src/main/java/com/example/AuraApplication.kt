package com.example

import android.app.Application
import com.example.data.local.AuraDatabase

class AuraApplication : Application() {

    lateinit var database: AuraDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AuraDatabase.getDatabase(this)
    }

    companion object {
        lateinit var instance: AuraApplication
            private set
    }
}
