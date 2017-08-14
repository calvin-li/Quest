package com.sandbox.calvin_li.quest

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
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

    companion object {
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

        val notBuild = Notification.Builder(this)
            .setContentTitle("5 New mails from " + "sender")
            .setContentText("subject")
            .setSmallIcon(R.drawable.notification_template_icon_bg)
            .setStyle(Notification.InboxStyle()
                .addLine("str1")
                .addLine("str2")
                .addLine("str3")
                .addLine("str4")
                .addLine("str5")
                .setBigContentTitle("Big Title")
                .setSummaryText("+3 more"))
            .build()

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(0, notBuild)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1, notBuild)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(2, notBuild)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(3, notBuild)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(4, notBuild)
    }

    private fun prepareListData() {
        var questStream: InputStream
        try {
            questStream = openFileInput(questFileName)
        } catch (ex: IOException) {
            questStream = resources.openRawResource(R.raw.quests)
        }
        //questStream = resources.openRawResource(R.raw.quests)
        questJson = Parser().parse(questStream) as JsonArray<JsonObject>
        questStream.close()
        saveJson(this)
    }
}
