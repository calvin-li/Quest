package com.sandbox.calvin_li.quest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView
import java.io.FileInputStream
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var listAdapter: ExpandableListAdapter
    lateinit var expandListView: MultiLevelListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expandListView = findViewById(R.id.top_view) as MultiLevelListView
        listAdapter = ExpandableListAdapter(this, prepareListData())
        expandListView.setAdapter(listAdapter)
    }

    private fun prepareListData(): JsonArray<JsonObject> {
        //val questFileName = "quests.json"
        val questStream: InputStream = resources.openRawResource(R.raw.quests)
        val quests = Parser().parse(questStream) as JsonArray<JsonObject>
        return quests
    }

}
