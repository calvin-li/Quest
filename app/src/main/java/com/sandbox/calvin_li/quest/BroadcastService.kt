package com.sandbox.calvin_li.quest

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BroadcastService: Service() {
    companion object {
        var bgService: BroadcastService? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bgService = this
        return START_STICKY
    }
}