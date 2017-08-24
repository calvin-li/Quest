package com.sandbox.calvin_li.quest

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.RemoteViews
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var listAdapter: ExpandableListAdapter
    lateinit var expandListView: MultiLevelListView

    internal companion object {
        val questFileName = "quests.json"
        lateinit var questJson: JsonArray<JsonObject>

        fun saveJson(_context: Context) {
            val writeStream: FileOutputStream = _context.openFileOutput(MainActivity.questFileName, Context
                .MODE_PRIVATE)
            writeStream.write(questJson.toJsonString().toByteArray())
            writeStream.close()
        }

        fun getNestedArray(indices: List<Int>): JsonArray<JsonObject> {
            var nestedArray: JsonArray<JsonObject> = questJson

            for (i in 0 until indices.size) {
                nestedArray =
                    nestedArray[indices[i]]["child"] as JsonArray<JsonObject>
            }
            return nestedArray
        }

        fun setNotifications(context: Context) {
            val mainIntent = Intent(context, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val groupNotification: Notification.Builder = Notification.Builder(context)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentIntent(pendingIntent)
                .setGroupSummary(true)
                .setGroup("g1")
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                -1, groupNotification.build())

            questJson.forEachIndexed { index, jsonObject ->
                val questPendingIntent =
                    NotificationActionReceiver.PendingIntentForAction(context)

                @Suppress("UNCHECKED_CAST")
                val subQuests: JsonArray<JsonObject> =
                    (jsonObject[MultiLevelListView.childLabel] as? JsonArray<JsonObject>) ?: JsonArray()
                var quest: String = jsonObject[MultiLevelListView.nameLabel] as String
                if(subQuests.count() > 0) {
                    quest = "$quest (+${subQuests.count()})"
                }

                val remoteView = RemoteViews(context.packageName, R.layout.notification_view)
                remoteView.setOnClickPendingIntent(R.id.notification_main_quest, questPendingIntent)
                remoteView.setTextViewText(R.id.notification_main_quest, quest)

                var allSubQuests = ""
                subQuests.forEach {
                    val subPendingIntent =
                        NotificationActionReceiver.PendingIntentForAction(context)

                    val subQuest: String = it[MultiLevelListView.nameLabel] as String
                    val subQuestRemote = RemoteViews(context.packageName, R.layout.notification_subquest)

                    subQuestRemote.setTextViewText(R.id.notification_subquest, subQuest)
                    subQuestRemote.setOnClickPendingIntent(R.id.notification_subquest, subPendingIntent)
                    remoteView.addView(R.id.notification_base, subQuestRemote)

                    // Newline displays as space.
                    allSubQuests +=  subQuest + ".\n"
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
                    .notify(index, notBuild.build())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expandListView = findViewById(R.id.top_view) as MultiLevelListView
        prepareListData()
        listAdapter = ExpandableListAdapter(this, null, null, emptyList())
        expandListView.setAdapter(listAdapter)

        setNotifications(this)
    }

    private fun prepareListData() {
        var questStream: InputStream = try {
            openFileInput(questFileName)!!
        } catch (ex: IOException) {
            resources.openRawResource(R.raw.quests)
        }
        //questStream = resources.openRawResource(R.raw.quests)
        questJson = Parser().parse(questStream) as JsonArray<JsonObject>
        questStream.close()
        saveJson(this)
    }
}
