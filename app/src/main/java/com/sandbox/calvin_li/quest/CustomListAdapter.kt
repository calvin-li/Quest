package com.sandbox.calvin_li.quest

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

class CustomListAdapter(
        context: Context,
        private var quests: Array<Quest> =
            flatten(MainActivity.questJson, emptyList()).toTypedArray())
    : ArrayAdapter<Quest>(context, R.layout.element_dialog, quests) {

    internal companion object {
        private const val indentSize = 24 // in dp

        private fun flatten(questJson: JsonArray<JsonObject>?, currentIndex: List<Int>)
                : List<Quest> {
            return questJson?.mapIndexed { index, jsonObject ->
                listOf(Quest(
                    jsonObject[Quest.nameLabel] as String,
                    currentIndex.plus(index),
                    jsonObject[Quest.expandLabel] as Boolean))
                        .plus(
                                @Suppress("UNCHECKED_CAST")
                                flatten(
                                        jsonObject[Quest.childLabel] as? JsonArray<JsonObject>,
                                        currentIndex.plus(index))
                        )
            }?.flatten() ?: emptyList()
        }
    }

    internal val onItemClickListener = AdapterView.OnItemClickListener {
        _: AdapterView<*>?, _: View?, position: Int, _: Long ->
        MainActivity.loadQuestJson(this.context)

        val quest = getItem(position)
        MainActivity.getNestedArray(quest.index)[Quest.expandLabel] = !(quest.expanded)

        MainActivity.saveJson(this.context)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val currentQuest = getItem(position)

        val container: View = convertView ?:
            (this.context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.element_header, parent, false)

        val indexPadding =
            context.resources.getDimension(R.dimen.subquest_left_padding).toInt() +
            indentSize * context.resources.displayMetrics.density * (currentQuest.index.size-1)
        container.setPadding(indexPadding.toInt(),
            container.paddingTop, container.paddingRight, container.paddingBottom)

        val questView = container.findViewById(R.id.element_header_text) as TextView

        val isHidden = checkIfHidden(currentQuest.index)

        MainActivity.loadQuestJson(this.context)
        if(MainActivity.getNestedArray(currentQuest.index)[Quest.expandLabel] as Boolean) {
            questView.setTextColor(Color.rgb(16,128,16))
        } else{
            questView.setTextColor(this.context.resources.getColor(R.color.colorPrimaryDark))
        }

        questView.text = currentQuest.name

        QuestOptionsDialogFragment.setAddButton(
                this, container.findViewById(R.id.element_header_add) as Button, currentQuest.index)

        QuestOptionsDialogFragment.setEditButton(
                this, container.findViewById(R.id.element_header_edit) as Button, questView.text, currentQuest.index)

        QuestOptionsDialogFragment.setDeleteButton(
                this, container.findViewById(R.id.element_header_delete) as Button, currentQuest.index)

        return container
    }

    private fun checkIfHidden(index: List<Int>): Boolean {

        val intProgression = (index.size - 1 downTo 0).toList()
        return intProgression
            .map { it -> index.subList(0, it) }
            .any { !(MainActivity.getNestedArray(it)[Quest.expandLabel] as Boolean) }
    }

    override fun notifyDataSetChanged() {
        MainActivity.loadQuestJson(this.context)
        quests = flatten(MainActivity.questJson, emptyList()).toTypedArray()
        super.notifyDataSetChanged()
    }

    override fun getCount(): Int = quests.count()

    override fun getItem(position: Int): Quest = quests[position]

    override fun getItemId(position: Int): Long {
        val currentQuest = getItem(position)
        return currentQuest.name.hashCode().toLong() * currentQuest.index.hashCode() //implicit cast
        // right operand
    }

    override fun getPosition(item: Quest?): Int = quests.indexOfFirst { it == item}
}