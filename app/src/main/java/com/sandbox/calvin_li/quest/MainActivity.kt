package com.sandbox.calvin_li.quest

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var listAdapter: ExpandableListAdapter
    private lateinit var expandListView: MultiLevelListView

    internal companion object {
        private val questFileName = "quests.json"
        lateinit var questJson: JsonArray<JsonObject>

        fun saveJson(context: Context) {
            val writeStream: FileOutputStream = context.openFileOutput(questFileName, Context.MODE_PRIVATE)
            writeStream.write(questJson.toJsonString().toByteArray())
            writeStream.close()
        }

        fun getNestedArray(indices: List<Int>): JsonObject {
            var nestedObject: JsonObject = questJson[indices[0]]

            for (i in 1 until indices.size) {
                nestedObject = (nestedObject[MultiLevelListView.childLabel] as JsonArray<JsonObject>)[indices[i]]
            }
            return nestedObject
        }

        internal fun loadQuestJson(context: Context){
            var questStream: InputStream = try {
                context.openFileInput(questFileName)!!
            } catch (ex: IOException) {
                context.resources.openRawResource(R.raw.quests)
            }
            // questStream = context.resources.openRawResource(R.raw.quests)
            questJson = Parser().parse(questStream) as JsonArray<JsonObject>
            questStream.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        expandListView = findViewById(R.id.top_view) as MultiLevelListView

        loadQuestJson(this)
        saveJson(this)

        listAdapter = ExpandableListAdapter(this, null, null, emptyList())
        expandListView.setAdapter(listAdapter)

        NotificationActionReceiver.createOverallNotification(this)

        val notificationIndexList: MutableList<List<QuestState>> = mutableListOf()
        (0 until questJson.size).forEach { notificationIndexList.add(listOf(QuestState(it, 0))) }
        NotificationActionReceiver.saveIndexList(this, notificationIndexList)

        NotificationActionReceiver.refreshNotifications(this)
    }
}
