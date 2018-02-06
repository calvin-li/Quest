package com.sandbox.calvin_li.quest

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json


class QuestState(
        var index: Int,
        val offset: Int
) {
    internal companion object {
        private val indexLabel = "index"
        private val offsetLabel = "offset"

        fun fromJsonObject(json: JsonObject): QuestState =
            QuestState(json[indexLabel] as Int, json[offsetLabel] as Int)
    }

    override fun toString(): String =
        json { JsonObject(mapOf(indexLabel to index, offsetLabel to offset)) }.toJsonString()
}