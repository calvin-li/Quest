package com.sandbox.calvin_li.quest

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

            for(index in 0 until questJson.size){
                NotificationActionReceiver.createNotification(context, emptyList(), index)
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
