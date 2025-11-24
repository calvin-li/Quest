package com.sandbox.calvin_li.quest

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json


class QuestState(
        var index: Int,
        val offset: Int
) {
    internal companion object {
        private const val INDEX_LABEL = "index"
        private const val OFFSET_LABEL = "offset"

        fun fromJsonObject(json: JsonObject): QuestState =
            QuestState(json[INDEX_LABEL] as Int, json[OFFSET_LABEL] as Int)
    }

    override fun toString(): String =
        json { JsonObject(mapOf(INDEX_LABEL to index, OFFSET_LABEL to offset)) }.toJsonString()
}