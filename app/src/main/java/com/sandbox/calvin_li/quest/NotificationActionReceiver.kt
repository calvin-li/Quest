package com.sandbox.calvin_li.quest

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.RemoteViews
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private var notificationMap: Int = 0
        private val add_action = "add_action"
        private val edit_action = "edit_action"
        private val delete_action = "delete_action"
        private val indexListFileName = "notificationIndexList.json"

        private fun nextActionNumber(): Int {
            return notificationMap++
        }

        internal fun saveIndexList(context: Context, notificationIndexList: List<List<Int>>){
            val indexArray = JsonArray<JsonArray<Int>>()
            notificationIndexList.forEach {
                indexArray.add(JsonArray(it))
            }
            val writeStream: FileOutputStream = context.openFileOutput(indexListFileName, Context.MODE_PRIVATE)
            writeStream.write(indexArray.toJsonString().toByteArray())
            writeStream.close()
        }

        internal fun getIndexList(context: Context): MutableList<List<Int>> {
            var indexStream: InputStream = try {
                context.openFileInput(indexListFileName)!!
            } catch (ex: IOException) {
                return mutableListOf()
            }
            val indexArray = Parser().parse(indexStream) as JsonArray<JsonArray<Int>>
            indexStream.close()
            return indexArray.map { it.toList() }.toMutableList()
        }

        private fun navigationPendingIntent(context: Context, indices: List<Int>, actionNumber: Int):
            PendingIntent {
            val questIntent = Intent("notification_action$actionNumber")
            questIntent.putExtra("isNav", true)
            questIntent.putExtra("indices", indices.toIntArray())
            return PendingIntent.getBroadcast(context, 0, questIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun buttonPendingIntent(context: Context, indices: List<Int>, actionNumber: Int)
            :PendingIntent {
            val questIntent = Intent("notification_action$actionNumber")
            questIntent.putExtra("isNav", false)
            questIntent.putExtra("indices", indices.toIntArray())
            return PendingIntent.getBroadcast(context, 0, questIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun createButtonAction(context: Context, intent: PendingIntent, key: String, label: String)
            : Notification.Action {
            val action = Notification.Action.Builder(
                Icon.createWithResource(context, R.mipmap.quest_notification),
                label,
                intent)

            if(!key.equals(delete_action, false)){
                val input: RemoteInput =
                    RemoteInput.Builder(key)
                        .build()
                action.addRemoteInput(input)
            }

            return action.build()
        }

        internal fun refreshNotifications(context: Context) {
            notificationMap = 0
            MainActivity.loadQuestJson(context)
            for (index in MainActivity.questJson.size - 1 downTo 0) {
                createQuestNotification(context, getIndexList(context)[index])
            }
            notificationMap = 0
        }

        internal fun removeAndShiftNotification(context: Context, indices: List<Int>) {
            val notificationIndexList = getIndexList(context)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(notificationIndexList.size - 1)
            (indices[0] + 1 until notificationIndexList.size).forEach {
                val newList: MutableList<Int> = notificationIndexList[it].toMutableList()
                newList[0] = newList[0] - 1
                notificationIndexList[it] = newList
            }
            notificationIndexList.removeAt(indices[0])
        }

        internal fun createOverallNotification(context: Context) {
            val groupNotification: Notification.Builder = Notification.Builder(context)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.mipmap.quest_notification)
                .setColor(context.getColor(R.color.goldStandard))
            .setGroupSummary(true)
                .setGroup("g1")
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                -1, groupNotification.build())
        }

        private fun createQuestNotification(context: Context, indices: List<Int>) {
            val jsonObject = MainActivity.getNestedArray(indices)
            val notificationNumber = indices.first()

            @Suppress("UNCHECKED_CAST")
            val subQuests: JsonArray<JsonObject> =
                (jsonObject[MultiLevelListView.childLabel] as? JsonArray<JsonObject>) ?: JsonArray()
            var quest: String = jsonObject[MultiLevelListView.nameLabel] as String
            if (subQuests.count() > 0) {
                quest = "$quest (+${subQuests.count()})"
            }

            val remoteView = RemoteViews(context.packageName, R.layout.notification_view)

            remoteView.setTextViewText(R.id.notification_main_quest, quest)

            val actionNumber = nextActionNumber()
            if (indices.size > 1) {
                val questPendingIntent = navigationPendingIntent(
                    context, indices.dropLast(1), actionNumber)
                remoteView.setOnClickPendingIntent(R.id.notification_main_base, questPendingIntent)
                remoteView.setTextViewText(R.id.notification_main_arrow, context.resources.getString(R.string.backward))
            } else {
                remoteView.setTextViewText(R.id.notification_main_arrow, "")
            }

            var allSubQuests = ""
            subQuests.forEachIndexed { index, subQuestJson ->
                val subQuest: String = subQuestJson[MultiLevelListView.nameLabel] as String
                val subQuestRemote = RemoteViews(context.packageName, R.layout.notification_subquest)
                subQuestRemote.setTextViewText(R.id.notification_subquest_text, subQuest)

                val subPendingIntent = navigationPendingIntent(context, indices.plus(index),
                    nextActionNumber())
                subQuestRemote.setOnClickPendingIntent(R.id.notification_subquest_base, subPendingIntent)

                val child = (subQuestJson[MultiLevelListView.childLabel] as? JsonArray<JsonObject>)
                    ?: JsonArray()
                if (child.isEmpty()) {
                    subQuestRemote.setTextViewText(R.id.notification_subquest_arrow, "")
                } else {
                    subQuestRemote.setTextViewText(R.id.notification_subquest_arrow, context.resources.getString(R.string.forward))
                }
                remoteView.addView(R.id.notification_base, subQuestRemote)

                // Newline displays as space.
                allSubQuests += subQuest + ".\n"
            }

            val buttonPendingIntent =
                buttonPendingIntent(context, indices, nextActionNumber())
            val deleteAction = createButtonAction(context, buttonPendingIntent, delete_action, "delete")
            val editAction = createButtonAction(context, buttonPendingIntent, edit_action, "edit")
            val addAction = createButtonAction(context, buttonPendingIntent, add_action, "add")

            val notBuild: Notification.Builder = Notification.Builder(context)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.mipmap.quest_notification)
                .setColor(context.getColor(R.color.goldStandard))
                .setContentTitle(quest)
                .setContentText(allSubQuests)
                .setCustomBigContentView(remoteView)
                .setActions(addAction, editAction, deleteAction)
                .setGroup("g1")
                .setStyle(Notification.DecoratedCustomViewStyle())

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(notificationNumber, notBuild.build())
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val indices = intent.getIntArrayExtra("indices").toList()
        val notificationIndexList: MutableList<List<Int>> = getIndexList(context)

        if(intent.getBooleanExtra("isNav", false)) {
            notificationIndexList[indices.first()] = indices
            saveIndexList(context, notificationIndexList)
        }
        else{
            val remoteInputBundle: Bundle? = RemoteInput.getResultsFromIntent(intent)
            if (remoteInputBundle == null) {
                // delete chosen
                if(indices.size == 1){
                    removeAndShiftNotification(context, indices)
                }else {
                    notificationIndexList[indices.first()] = indices.dropLast(1)
                }
                saveIndexList(context, notificationIndexList)

                QuestOptionsDialogFragment.deleteQuest(indices, context)
            } else {
                var input: CharSequence? = remoteInputBundle.getCharSequence(add_action)
                if(input == null){
                    input = remoteInputBundle.getCharSequence(edit_action)
                    QuestOptionsDialogFragment.editQuest(indices, input.toString(), context)
                } else{
                    QuestOptionsDialogFragment.addSubQuest(indices, input.toString(), context)
                }
            }
        }
        refreshNotifications(context)
    }
 }