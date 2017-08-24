package com.sandbox.calvin_li.quest

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver: BroadcastReceiver() {

    companion object {
        fun PendingIntentForAction(context: Context): PendingIntent {
            val questIntent = Intent("notification_action")
            return PendingIntent.getBroadcast(context, 0, questIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        intent.getIntArrayExtra("coordinates")
    }
}