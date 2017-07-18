package com.sandbox.calvin_li.quest

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView
import de.jupf.staticlog.Log
import java.io.*

class MainActivity : AppCompatActivity() {
    lateinit var listAdapter: ExpandableListAdapter
    lateinit var expandListView: MultiLevelListView

    companion object {
        val questFileName = "quests.json"
        lateinit var questJson: JsonArray<JsonObject>
        fun saveJson(_context: Context){
            val writeStream: FileOutputStream = _context.openFileOutput(MainActivity.questFileName, Context
                    .MODE_PRIVATE)
            writeStream.write(questJson.toJsonString().toByteArray())
            writeStream.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expandListView = findViewById(R.id.top_view) as MultiLevelListView
        prepareListData()
        listAdapter = ExpandableListAdapter(this, questJson)
        expandListView.setAdapter(listAdapter)
    }

    private fun prepareListData() {
        var questStream: InputStream
        try{
            questStream = openFileInput(questFileName)
        } catch (ex: IOException){
            questStream = resources.openRawResource(R.raw.quests)
        }
        //questStream = resources.openRawResource(R.raw.quests)
        questJson = Parser().parse(questStream) as JsonArray<JsonObject>
        questStream.close()
    }
}
