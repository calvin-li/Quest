package com.sandbox.calvin_li.quest

import android.content.Context
import android.widget.ArrayAdapter
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

typealias Q = Pair<String, List<Int>>

class CustomListAdapter(
    context: Context,
    private val quests: Array<Q> =
        flatten(MainActivity.questJson, emptyList()).toTypedArray())
    : ArrayAdapter<Q>(context, R.id.element_header_container, quests) {

    internal companion object {

        fun flatten(questJson: JsonArray<JsonObject>?, currentIndex: List<Int>)
            : List<Q> {
            return questJson?.mapIndexed { index, jsonObject ->
                listOf(Pair(
                    jsonObject[MultiLevelListView.nameLabel] as String,
                    currentIndex.plus(index)))
                .plus(
                    @Suppress("UNCHECKED_CAST")
                    flatten(
                        jsonObject[MultiLevelListView.childLabel] as? JsonArray<JsonObject>,
                        currentIndex.plus(index))
                )
            }?.flatten() ?: emptyList()
        }
    }

    override fun getCount(): Int = quests.count()
}