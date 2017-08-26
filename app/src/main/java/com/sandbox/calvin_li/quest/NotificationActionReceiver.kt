package com.sandbox.calvin_li.quest

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView

class NotificationActionReceiver: BroadcastReceiver() {

    companion object {
        private fun PendingIntentForAction(context: Context, indices: List<Int>, next: Int):
            PendingIntent {
            val questIntent = Intent("notification_action")
            questIntent.putExtra("indices", indices.toIntArray())
            questIntent.putExtra("next", next)
            return PendingIntent.getBroadcast(context, 0, questIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        internal fun createNotification(context: Context, indices: List<Int>, next: Int) {
            val jsonObject = MainActivity.getNestedArray(indices)[next]

            @Suppress("UNCHECKED_CAST")
            val subQuests: JsonArray<JsonObject> =
                (jsonObject[MultiLevelListView.childLabel] as? JsonArray<JsonObject>) ?: JsonArray()
            var quest: String = jsonObject[MultiLevelListView.nameLabel] as String
            if (subQuests.count() > 0) {
                quest = "$quest (+${subQuests.count()})"
            }

            val remoteView = RemoteViews(context.packageName, R.layout.notification_view)
            remoteView.setTextViewText(R.id.notification_main_quest, quest)
            if(!indices.isEmpty()) {
                val questPendingIntent =
                    PendingIntentForAction(context, indices.dropLast(1), indices.last())
                remoteView.setOnClickPendingIntent(R.id.notification_main_quest, questPendingIntent)
            }

            var allSubQuests = ""
            subQuests.forEachIndexed {index, subQuestJson ->
                val subQuest: String = subQuestJson[MultiLevelListView.nameLabel] as String
                val subQuestRemote = RemoteViews(context.packageName, R.layout.notification_subquest)
                subQuestRemote.setTextViewText(R.id.notification_subquest, subQuest)

                if(subQuestJson.containsKey(MultiLevelListView.childLabel)) {
                    val subPendingIntent =
                        PendingIntentForAction(context, indices.plus(next), index)
                    subQuestRemote.setOnClickPendingIntent(R.id.notification_subquest, subPendingIntent)
                }
                remoteView.addView(R.id.notification_base, subQuestRemote)

                // Newline displays as space.
                allSubQuests += subQuest + ".\n"
            }

            val notBuild: Notification.Builder = Notification.Builder(context)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentTitle(quest)
                .setContentText(allSubQuests)
                .setCustomBigContentView(remoteView)
                .setGroup("g1")
                .setStyle(Notification.DecoratedCustomViewStyle())

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(indices.firstOrNull() ?: next, notBuild.build())
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val indices = intent.getIntArrayExtra("indices").toList()
        val next = intent.getIntExtra("next", -1)
        createNotification(context, indices, next)
    }
}