package com.sandbox.calvin_li.quest

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json


class QuestState(
        var index: Int,
        val page: Int
) {
    internal companion object {
        private val indexLabel = "index"
        private val pageLabel = "page"

        fun fromJsonObject(json: JsonObject): QuestState =
            QuestState(json[indexLabel] as Int, json[pageLabel] as Int)
    }

    override fun toString(): String {
        return json { JsonObject(mapOf(indexLabel to index, pageLabel to page)) }.toJsonString()
    }
}