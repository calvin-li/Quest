package com.sandbox.calvin_li.quest

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.RemoteViews
import android.widget.TextView
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expandListView = findViewById(R.id.top_view) as MultiLevelListView
        prepareListData()
        listAdapter = ExpandableListAdapter(this, null, null, emptyList())
        expandListView.setAdapter(listAdapter)

        setNotifications()
    }

    private fun setNotifications() {
        val notManager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        val groupNotification: Notification.Builder = Notification.Builder(this)
            .setOngoing(true)
            .setShowWhen(false)
            .setSmallIcon(R.drawable.notification_template_icon_bg)
            .setContentTitle("Summary")
            .setContentText("subject")
            .setGroupSummary(true)
            .setGroup("g1")
        notManager.notify(-1, groupNotification.build())

        questJson.forEachIndexed { index, jsonObject ->
            val quest: String = jsonObject[MultiLevelListView.nameLabel] as String
            @Suppress("UNCHECKED_CAST")
            val subQuests: JsonArray<JsonObject> =
                (jsonObject[MultiLevelListView.childLabel] as? JsonArray<JsonObject>) ?: JsonArray()
            var subQuestString: String = ""
            subQuests.forEach {
                subQuestString += it[MultiLevelListView.nameLabel] as String + ".\n"
                // Newline displays as space.
            }

            val remoteView: RemoteViews = RemoteViews(this.packageName, R.layout.notification_view)
            val remoteQuest = RemoteViews(this.packageName, R.layout.notification_main_quest)
            remoteQuest.setTextViewText(R.id.notification_quest, quest)
            remoteView.addView(R.id.notification_base, remoteQuest)

            val notBuild: Notification.Builder = Notification.Builder(this)
                .setOngoing(true)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentTitle(quest)
                .setContentText("+${subQuests.count()} $subQuestString")
                .setCustomBigContentView(remoteView)
                .setGroup("g1")
                .setStyle(Notification.DecoratedCustomViewStyle())

            notManager.notify(index, notBuild.build())
        }
    }

    private fun prepareListData() {
        var questStream: InputStream
        try {
            questStream = openFileInput(questFileName)!!
        } catch (ex: IOException) {
            questStream = resources.openRawResource(R.raw.quests)
        }
        //questStream = resources.openRawResource(R.raw.quests)
        questJson = Parser().parse(questStream) as JsonArray<JsonObject>
        questStream.close()
        saveJson(this)
    }
}
