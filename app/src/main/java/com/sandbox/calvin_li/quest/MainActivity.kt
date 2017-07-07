package com.sandbox.calvin_li.quest

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    lateinit var listAdapter: ExpandableListAdapter
    lateinit var expandListView: MultiLevelListView

    companion object {
        val questFileName = "quests.json"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expandListView = findViewById(R.id.top_view) as MultiLevelListView
        listAdapter = ExpandableListAdapter(this, prepareListData())
        expandListView.setAdapter(listAdapter)
    }

    private fun prepareListData(): JsonArray<JsonObject> {
        val questStream: InputStream = resources.openRawResource(R.raw.quests)
        //val questStream: FileInputStream = openFileInput(questFileName)
        val quests = Parser().parse(questStream) as JsonArray<JsonObject>
        questStream.close()
        return quests
    }
}
