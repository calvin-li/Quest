package com.sandbox.calvin_li.quest

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ListView
import android.widget.Toast
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var questView: ListView

    internal companion object {
        private const val questFileName = "quests.json"
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
                nestedObject = (nestedObject[Quest.childLabel] as JsonArray<JsonObject>)[indices[i]]
            }
            return nestedObject
        }

        internal fun loadQuestJson(context: Context){
            val questStream: InputStream = try {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.action_add){
            val editView = QuestOptionsDialogFragment.getDialogView(this)
            editView.hint = "Add new subquest here"

            val dialog = QuestOptionsDialogFragment.createDialog(this, editView, "Add subquest", { _, _ ->
                loadQuestJson(this)

                val newObject = JsonObject()
                newObject[Quest.nameLabel] = editView.text.toString()
                newObject[Quest.expandLabel] = true

                questJson.add(newObject)
                saveJson(this)
                this.onResume()
            })

            dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
            dialog.show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }

    override fun onResume() {
        super.onResume()
        loadQuestJson(this)
        saveJson(this)

        NotificationActionReceiver.createOverallNotification(this)
        val notificationIndexList: MutableList<List<QuestState>> = mutableListOf()
        (0 until questJson.size).forEach { notificationIndexList.add(listOf(QuestState(it, 0))) }
        NotificationActionReceiver.saveIndexList(this, notificationIndexList)
        NotificationActionReceiver.refreshNotifications(this)

        questView = findViewById(R.id.top_view) as ListView
        questView.isSmoothScrollbarEnabled = true
        val adapter = CustomListAdapter(this)
        questView.adapter = adapter
        questView.onItemClickListener = adapter.onItemClickListener
    }
}
