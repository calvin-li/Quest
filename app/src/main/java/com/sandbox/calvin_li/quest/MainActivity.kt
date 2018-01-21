package com.sandbox.calvin_li.quest

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var questView: ListView

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
                @Suppress("UNCHECKED_CAST")
                nestedObject = (nestedObject[CustomListAdapter.childLabel] as JsonArray<JsonObject>)[indices[i]]
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
            @Suppress("UNCHECKED_CAST")
            questJson = Parser().parse(questStream) as JsonArray<JsonObject>
            questStream.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)
        loadQuestJson(this)
        saveJson(this)

        NotificationActionReceiver.createOverallNotification(this)
        val notificationIndexList: MutableList<List<QuestState>> = mutableListOf()
        (0 until questJson.size).forEach { notificationIndexList.add(listOf(QuestState(it, 0))) }
        NotificationActionReceiver.saveIndexList(this, notificationIndexList)
        NotificationActionReceiver.refreshNotifications(this)
    }

    override fun onResume() {
        super.onResume()
        loadQuestJson(this)

        questView = findViewById(R.id.top_view) as ListView
        val adapter = CustomListAdapter(this)
        questView.adapter = adapter
        questView.onItemClickListener = adapter.onItemClickListener
    }
}
