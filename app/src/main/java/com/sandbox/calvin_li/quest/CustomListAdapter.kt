package com.sandbox.calvin_li.quest

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

class CustomListAdapter(
        context: Context,
        private val quests: Array<Pair<String, List<Int>>> =
        flatten(MainActivity.questJson, emptyList()).toTypedArray())
    : ArrayAdapter<Pair<String, List<Int>>>(context, R.layout.element_dialog, quests) {

    internal companion object {

        fun flatten(questJson: JsonArray<JsonObject>?, currentIndex: List<Int>)
                : List<Pair<String, List<Int>>> {
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

    override fun getItem(position: Int): Pair<String, List<Int>> = quests[position]

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return item.first.hashCode().toLong() * item.second.hashCode() //implicit cast right operand
    }

    override fun getPosition(item: Pair<String, List<Int>>?): Int =
        quests.indexOfFirst { it == item}

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val returnedView: View = convertView ?: (this.context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.element_header, parent, false)

        val labelListHeader = returnedView.findViewById(R.id.element_header_text) as TextView
        labelListHeader.setTypeface(null, Typeface.BOLD)

        labelListHeader.tag = position
        labelListHeader.text = getItem(position).first

        return returnedView
    }
}