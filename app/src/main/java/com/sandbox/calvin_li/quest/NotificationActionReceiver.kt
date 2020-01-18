package com.sandbox.calvin_li.quest

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val add_action = "add_action"
        private const val edit_action = "edit_action"
        private const val delete_action = "delete_action"
        private const val indexListFileName = "notificationIndexList.json"
        private const val subQuestsPerPage: Int = 5 // Number excluding paging buttons, two less than full size
        internal const val channelId = "Quests"

        internal fun saveIndexList(context: Context, notificationIndexList: List<List<QuestState>>){
            val indexArray = JsonArray<JsonArray<QuestState>>()
            notificationIndexList.forEach {
                indexArray.add(JsonArray(it))
            }
            val writeStream: FileOutputStream = context.openFileOutput(indexListFileName, Context.MODE_PRIVATE)
            writeStream.write(indexArray.toJsonString().toByteArray())
            writeStream.close()
        }

        internal fun getIndexList(context: Context): MutableList<List<QuestState>> {
            val indexStream: InputStream = try {
                context.openFileInput(indexListFileName)!!
            } catch (ex: IOException) {
                return mutableListOf()
            }
            @Suppress("UNCHECKED_CAST")
            val indexArray = Parser().parse(indexStream) as JsonArray<JsonArray<JsonObject>>
            indexStream.close()
            return indexArray.map { i -> i.toList().map { j -> QuestState.fromJsonObject(j) } }
                .toMutableList()
        }

        private fun setIntentExtras(questIntent: Intent, indices: List<QuestState>) {
            questIntent.action = Random().nextLong().toString()
            questIntent.putExtra("indices", indices.map { it.index }.toIntArray())
            questIntent.putExtra("offsets", indices.map { it.offset }.toIntArray())
        }

        private fun navigationPendingIntent(context: Context, indices: List<QuestState>):
                PendingIntent {
            val questIntent = Intent(context, NotificationActionReceiver::class.java)
            setIntentExtras(questIntent, indices)
            questIntent.putExtra("isNav", true)
            return PendingIntent.getBroadcast(context, 0, questIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun buttonPendingIntent(context: Context, indices: List<QuestState>)
            :PendingIntent {
            val questIntent = Intent(context, NotificationActionReceiver::class.java)
            setIntentExtras(questIntent, indices)
            return PendingIntent.getBroadcast(context, 0, questIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun createButtonAction(context: Context, intent: PendingIntent, key: String, label:
        String, quest: String = "")
            : Notification.Action {
            val action = Notification.Action.Builder(
                Icon.createWithResource(context, R.mipmap.quest_notification),
                label,
                intent)

            if(!key.equals(delete_action, false)){
                val hint: String = if(key.equals(add_action, false)){
                    context.resources.getString(R.string.quest_add_hint)
                } else {    //Edit action
                    quest
                }

                val input: RemoteInput =
                    RemoteInput.Builder(key)
                        .setLabel(hint)
                        .build()
                action.addRemoteInput(input)
            }

            return action.build()
        }

        internal fun refreshNotifications(context: Context) {
            clearNotifications(context)

            MainActivity.loadQuestJson(context)
            val indexList = getIndexList(context)
            // notifications appear in reverse order of creation
            MainActivity.questJson.reversed().forEachIndexed{
                i, _ -> createQuestNotification(context, indexList.reversed()[i])
            }
        }

        private fun clearNotifications(context: Context) {
            val numNotifications = getIndexList(context).size
            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notifications = notificationManager.activeNotifications
            notifications.forEach { n ->
                if (n.id >= numNotifications)
                notificationManager.cancel(n.id)
            }
        }

        internal fun removeAndShiftNotification(context: Context, index: Int) {
            val notificationIndexList = getIndexList(context)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(notificationIndexList.size - 1)

            (index + 1 until notificationIndexList.size).forEach {
                val newList: MutableList<QuestState> = notificationIndexList[it].toMutableList()
                newList[0].index = newList.first().index - 1
                notificationIndexList[it] = newList
            }

            notificationIndexList.removeAt(index)
            saveIndexList(context, notificationIndexList)
        }

        internal fun createOverallNotification(context: Context) {
            val groupNotification: Notification.Builder = Notification.Builder(context, channelId)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.mipmap.quest_notification)
                .setColor(context.getColor(R.color.groovy_notification))
                .setGroupSummary(true)
                .setGroup("g1")
                .setContentIntent(PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java), 0))

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                -1, groupNotification.build())
        }

        private fun createQuestNotification(context: Context, indices: List<QuestState>) {
            val jsonObject = MainActivity.getNestedArray(indices.map { it.index })
            val notificationNumber = indices.first().index
            var subQuestsOnThisPage = subQuestsPerPage

            @Suppress("UNCHECKED_CAST")
            val subQuestsNonPaged =
                    (jsonObject[Quest.childLabel] as? JsonArray<JsonObject>) ?: JsonArray()

            var offset = indices.last().offset
            val previousPageExists = offset > 1
            if(!previousPageExists){
                subQuestsOnThisPage += 1
                offset = 0
            }
            val nextPageExists = offset + subQuestsOnThisPage + 1 < subQuestsNonPaged.size
            if(!nextPageExists){
                subQuestsOnThisPage += 1
            }

            val subQuests: List<JsonObject> = if(subQuestsNonPaged.size > subQuestsPerPage + 2){
                subQuestsNonPaged.subList(offset, minOf(offset + subQuestsOnThisPage, subQuestsNonPaged.size))
            } else{
                subQuestsNonPaged
            }

            val questRaw: String = jsonObject[Quest.nameLabel] as String

            val quest = questRaw +
                if (subQuests.count() > 0) {
                    "(+${subQuestsNonPaged.count()})"
                } else { "" }

            val remoteView = RemoteViews(context.packageName, R.layout.notification_view)
            remoteView.setTextViewText(R.id.notification_main_quest, quest)
            // Could not do this through night mode styles
            if (MainActivity.inNightMode(context)){
                //remoteView.setTextColor(R.id.notification_main_quest, Color.WHITE)
            }

            if (indices.size > 1) {
                val questPendingIntent = navigationPendingIntent(
                    context, indices.dropLast(1))
                remoteView.setOnClickPendingIntent(R.id.notification_main_base, questPendingIntent)
                remoteView.setTextViewText(R.id.notification_main_arrow, context.resources.getString(R.string.backward))
            } else {
                remoteView.setTextViewText(R.id.notification_main_arrow, "")
            }

            if(previousPageExists){
                val newOffset = maxOf(offset - subQuestsPerPage, 0)
                addPagingQuest(context, indices, remoteView, newOffset)
            }

            var allSubQuests = ""
            subQuests.forEachIndexed { index, subQuestJson ->
                val subQuest: String = subQuestJson[Quest.nameLabel] as String
                val subQuestRemote = RemoteViews(context.packageName, R.layout.notification_subquest)
                subQuestRemote.setTextViewText(R.id.notification_subquest_text, subQuest)
                if (MainActivity.inNightMode(context)){
                    //subQuestRemote.setTextColor(R.id.notification_subquest_text, Color.WHITE)
                }

                val subPendingIntent = navigationPendingIntent(
                    context, indices.plus(QuestState(index + offset,0)))
                subQuestRemote.setOnClickPendingIntent(R.id.notification_subquest_base, subPendingIntent)

                @Suppress("UNCHECKED_CAST")
                val child =
                        subQuestJson[Quest.childLabel] as? JsonArray<JsonObject> ?: JsonArray()
                if (child.isEmpty()) {
                    subQuestRemote.setTextViewText(R.id.notification_subquest_arrow, "")
                } else {
                    subQuestRemote.setTextViewText(R.id.notification_subquest_arrow, context.resources.getString(R.string.forward))
                }
                remoteView.addView(R.id.notification_base, subQuestRemote)

                // Newline displays as space.
                allSubQuests += "$subQuest.\n"
            }

            if(nextPageExists){
                val newOffset = minOf(offset + subQuestsOnThisPage, subQuestsNonPaged.size)
                addPagingQuest(context, indices, remoteView, newOffset)
            }

            val buttonPendingIntent =
                buttonPendingIntent(context, indices)
            val deleteAction = createButtonAction(context, buttonPendingIntent, delete_action, "delete")
            val editAction = createButtonAction(context, buttonPendingIntent, edit_action, "edit", questRaw)
            val addAction = createButtonAction(context, buttonPendingIntent, add_action, "add")

            val notBuild: Notification.Builder = Notification.Builder(context, channelId)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.mipmap.quest_notification)
                .setColor(context.getColor(R.color.groovy_notification))
                .setContentTitle(quest)
                .setContentText(allSubQuests)
                .setCustomBigContentView(remoteView)
                .setActions(addAction, editAction, deleteAction)
                .setGroup("g1")
                .setStyle(Notification.DecoratedCustomViewStyle())

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(notificationNumber, notBuild.build())
        }

        private fun addPagingQuest(context: Context, indices: List<QuestState>, remoteView: RemoteViews, newOffset: Int) {
            val previous = newOffset < indices.last().offset
            val label = if(previous){"Previous"} else{"Next"}
            val subQuestRemote = RemoteViews(context.packageName, R.layout.notification_subquest)

            subQuestRemote.setTextViewText(R.id.notification_subquest_text, label)
            val adjustedIndices = indices.map {
                if (it == indices.last()) {
                    QuestState(it.index, newOffset)
                } else {
                    it
                }
            }
            val subPendingIntent = navigationPendingIntent(
                context, adjustedIndices)
            subQuestRemote.setOnClickPendingIntent(R.id.notification_subquest_base, subPendingIntent)
            remoteView.addView(R.id.notification_base, subQuestRemote)
            val arrow = if(previous){R.string.up}else{R.string.down}
            subQuestRemote.setTextViewText(R.id.notification_subquest_arrow, context.resources.getString(arrow))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.w("boot_broadcast_poc", "starting service...")
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            val indices = intent.getIntArrayExtra("indices").toList()
            val offsets = intent.getIntArrayExtra("offsets").toList()
            val notificationIndexList = getIndexList(context)

            if (intent.getBooleanExtra("isNav", false)) {
                notificationIndexList[indices.first()] =
                        indices.zip(offsets).map { QuestState(it.first, it.second) }
                saveIndexList(context, notificationIndexList)
            } else {
                val remoteInputBundle: Bundle? = RemoteInput.getResultsFromIntent(intent)
                if (remoteInputBundle == null) {
                    // assume delete
                    QuestOptionsDialogFragment.deleteQuest(indices, context)
                } else {
                    var input: CharSequence? = remoteInputBundle.getCharSequence(add_action)
                    if (input == null) {
                        input = remoteInputBundle.getCharSequence(edit_action)
                        QuestOptionsDialogFragment.editQuest(indices, input.toString(), context)
                    } else {
                        QuestOptionsDialogFragment.addSubQuest(indices, input.toString(), context)
                    }
                }
            }
        }
        refreshNotifications(context)
    }
 }