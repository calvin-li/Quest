package com.sandbox.calvin_li.quest

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.widget.RemoteViews
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private val numActions: Int = 100
        private var notificationMap = arrayOfNulls<Int?>(numActions)

        private fun PendingIntentForAction(context: Context, indices: List<Int>, next: Int, actionNumber: Int):
            PendingIntent {
            val questIntent = Intent("notification_action$actionNumber")
            questIntent.putExtra("indices", indices.toIntArray())
            questIntent.putExtra("next", next)
            return PendingIntent.getBroadcast(context, 0, questIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        internal fun createOverallNotification(context: Context) {
            val groupNotification: Notification.Builder = Notification.Builder(context)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setGroupSummary(true)
                .setGroup("g1")
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                -1, groupNotification.build())
        }

        private fun clearNotificationActions(index: Int){
            notificationMap = notificationMap.mapIndexed({ _, mapping ->
                if(mapping == index){
                    null
                } else{
                    mapping
                }
            }).toTypedArray()
        }

        private fun nextActionNumber(notificationNumber: Int): Int {
            val nextNumber = notificationMap.indexOf(null) // -1 if no elements are null
            notificationMap[nextNumber] = notificationNumber
            return nextNumber
        }

        private fun createButtonAction(context: Context, intent: PendingIntent, key: String, label: String)
                : Notification.Action {
            val deleteInput =
                    RemoteInput.Builder(key)
                            .setLabel(label)
                            .build()
            val deleteAction: Notification.Action =
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.abc_ic_arrow_drop_right_black_24dp),
                    label,
                    intent)
                    .addRemoteInput(deleteInput)
                .build()

            return deleteAction
        }

        internal fun createQuestNotification(context: Context, indices: List<Int>, next: Int) {
            val jsonObject = MainActivity.getNestedArray(indices)[next]
            val notificationNumber = indices.firstOrNull() ?: next

            clearNotificationActions(notificationNumber)

            @Suppress("UNCHECKED_CAST")
            val subQuests: JsonArray<JsonObject> =
                (jsonObject[MultiLevelListView.childLabel] as? JsonArray<JsonObject>) ?: JsonArray()
            var quest: String = jsonObject[MultiLevelListView.nameLabel] as String
            if (subQuests.count() > 0) {
                quest = "$quest (+${subQuests.count()})"
            }

            val remoteView = RemoteViews(context.packageName, R.layout.notification_view)

            remoteView.setTextViewText(R.id.notification_main_quest, quest)

            var questPendingIntent: PendingIntent
            val actionNumber = nextActionNumber(notificationNumber)
            if (!indices.isEmpty()) {
                questPendingIntent = PendingIntentForAction(
                    context, indices.dropLast(1), indices.last(), actionNumber)
                remoteView.setOnClickPendingIntent(R.id.notification_main_base, questPendingIntent)
                remoteView.setTextViewText(R.id.notification_main_arrow, context.resources.getString(R.string.backward))
            } else {
                questPendingIntent = PendingIntentForAction(
                    context, indices, next, actionNumber)
                remoteView.setTextViewText(R.id.notification_main_arrow, "")
            }

            var allSubQuests = ""
            subQuests.forEachIndexed { index, subQuestJson ->
                val subQuest: String = subQuestJson[MultiLevelListView.nameLabel] as String
                val subQuestRemote = RemoteViews(context.packageName, R.layout.notification_subquest)
                subQuestRemote.setTextViewText(R.id.notification_subquest_text, subQuest)

                if (subQuestJson.containsKey(MultiLevelListView.childLabel)) {
                    val subPendingIntent =
                        PendingIntentForAction(context, indices.plus(next), index, nextActionNumber(notificationNumber))
                    subQuestRemote.setOnClickPendingIntent(R.id.notification_subquest_base, subPendingIntent)
                    subQuestRemote.setTextViewText( R.id.notification_subquest_arrow, context.resources.getString(R.string.forward))
                } else {
                    subQuestRemote.setTextViewText(R.id.notification_subquest_arrow, "")
                }
                remoteView.addView(R.id.notification_base, subQuestRemote)

                // Newline displays as space.
                allSubQuests += subQuest + ".\n"
            }

            val deleteAction = createButtonAction(context, questPendingIntent, "delete", "delete")
            val editAction = createButtonAction(context, questPendingIntent, "edit", "edit")

            val notBuild: Notification.Builder = Notification.Builder(context)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentTitle(quest)
                .setContentText(allSubQuests)
                .setCustomBigContentView(remoteView)
                .setActions(deleteAction, editAction)
                .setGroup("g1")
                .setStyle(Notification.DecoratedCustomViewStyle())

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(notificationNumber, notBuild.build())
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val indices = intent.getIntArrayExtra("indices").toList()
        val next = intent.getIntExtra("next", -1)
        createQuestNotification(context, indices, next)
    }
}