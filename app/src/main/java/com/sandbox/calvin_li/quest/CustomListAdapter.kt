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
import com.beust.klaxon.convert

class CustomListAdapter(
        context: Context,
        private var quests: Array<Pair<String, List<Int>>> =
            flatten(MainActivity.questJson, emptyList()).toTypedArray())
    : ArrayAdapter<Pair<String, List<Int>>>(context, R.layout.element_dialog, quests) {

    internal companion object {
        private val indentSize = 24 // in dp
        internal val nameLabel: String = "name"
        internal val hiddenLabel: String = "hidden"
        internal val childLabel: String = "child"

        fun flatten(questJson: JsonArray<JsonObject>?, currentIndex: List<Int>)
                : List<Pair<String, List<Int>>> {
            return questJson?.mapIndexed { index, jsonObject ->
                listOf(Pair(
                        jsonObject[nameLabel] as String,
                        currentIndex.plus(index)))
                        .plus(
                                @Suppress("UNCHECKED_CAST")
                                flatten(
                                        jsonObject[childLabel] as? JsonArray<JsonObject>,
                                        currentIndex.plus(index))
                        )
            }?.flatten() ?: emptyList()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val (name, index) = getItem(position)

        val returnedView: View = convertView ?: (this.context.getSystemService(Context
            .LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.element_header, parent, false)

        val indexPadding =
            context.resources.getDimension(R.dimen.subquest_left_padding).toInt() +
            indentSize * context.resources.displayMetrics.density * (index.size-1)
        returnedView.setPadding(indexPadding.toInt(),
            returnedView.paddingTop, returnedView.paddingRight, returnedView.paddingBottom)

        val questView = returnedView.findViewById(R.id.element_header_text) as TextView
        questView.setTypeface(null, Typeface.BOLD)
        questView.text = name

        QuestOptionsDialogFragment.setAddButton(
                this, returnedView.findViewById(R.id.element_header_add) as Button, index)

        QuestOptionsDialogFragment.setEditButton(
                this, returnedView.findViewById(R.id.element_header_edit) as Button, questView.text, index)

        QuestOptionsDialogFragment.setDeleteButton(
                this, returnedView.findViewById(R.id.element_header_delete) as Button, index)

        return returnedView
    }

    override fun notifyDataSetChanged() {
        MainActivity.loadQuestJson(this.context)
        quests = flatten(MainActivity.questJson, emptyList()).toTypedArray()
        super.notifyDataSetChanged()
    }

    override fun getCount(): Int = quests.count()

    override fun getItem(position: Int): Pair<String, List<Int>> = quests[position]

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return item.first.hashCode().toLong() * item.second.hashCode() //implicit cast right operand
    }

    override fun getPosition(item: Pair<String, List<Int>>?): Int = quests.indexOfFirst { it == item}
}