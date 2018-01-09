package com.sandbox.calvin_li.quest

import android.content.Context
import android.widget.ArrayAdapter
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

class CustomListAdapter(context: Context)
    : ArrayAdapter<JsonObject>(context, R.id.element_header_container, MainActivity.questJson) {
    private val quests: JsonArray<JsonObject> = MainActivity.questJson

    override fun getCount(): Int = quests.count()
}