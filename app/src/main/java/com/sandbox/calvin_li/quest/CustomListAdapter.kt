package com.sandbox.calvin_li.quest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

class CustomListAdapter(
        context: Context,
        private var quests: Array<Quest> =
            flatten(MainActivity.questJson, emptyList()).toTypedArray()
        )
    : ArrayAdapter<Quest>(context, R.layout.element_dialog, quests) {
    internal companion object {
        private const val indentSize = 24 // in dp

        private fun flatten(questJson: JsonArray<JsonObject>?, currentIndex: List<Int>)
                : List<Quest> {
            return questJson?.mapIndexed { index, jsonObject ->
                listOf(Quest(
                    jsonObject[Quest.nameLabel] as String,
                    currentIndex.plus(index),
                    jsonObject[Quest.expandLabel] as Boolean,
                    isHidden(currentIndex)))
                        .plus(
                                @Suppress("UNCHECKED_CAST")
                                flatten(
                                        jsonObject[Quest.childLabel] as? JsonArray<JsonObject>,
                                        currentIndex.plus(index))
                        )
            }?.flatten() ?: emptyList()
        }

        private fun isHidden(index: List<Int>) =
            (index.size - 1 downTo 0).toList()
                .mapIndexed{ it, _ -> index.subList(0, it+1) }
                .any { !(MainActivity.getNestedArray(it)[Quest.expandLabel] as Boolean) }
    }

    private var visibleQuests = quests.filter { !it.hidden }
        get() = quests.filter { !it.hidden }

    internal val onItemClickListener = AdapterView.OnItemClickListener {
        adapterView: AdapterView<*>?, _: View?, position: Int, _: Long ->
        MainActivity.loadQuestJson(this.context)

        val quest = getItem(position)
        MainActivity.getNestedArray(quest.index)[Quest.expandLabel] = !(quest.expanded)

        MainActivity.saveJson(this.context)
        notifyDataSetChanged()

        (adapterView as ListView).setSelection(position)
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

        if(currentQuest.hidden) {
            container.visibility = View.GONE
        } else{
            container.visibility = View.VISIBLE
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

    override fun notifyDataSetChanged() {
        MainActivity.loadQuestJson(this.context)
        quests = flatten(MainActivity.questJson, emptyList()).toTypedArray()
        super.notifyDataSetChanged()
    }

    override fun getCount(): Int = visibleQuests.count()

    override fun getItem(position: Int): Quest = visibleQuests[position]

    override fun getItemId(position: Int): Long {
        val currentQuest = getItem(position)
        return currentQuest.name.hashCode().toLong() * currentQuest.index.hashCode()
        //implicit cast right operand
    }

    override fun getPosition(item: Quest?): Int = visibleQuests.indexOfFirst {it == item}
}