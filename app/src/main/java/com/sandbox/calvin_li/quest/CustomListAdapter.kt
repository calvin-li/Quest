package com.sandbox.calvin_li.quest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import kotlin.math.max

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
                    jsonObject[Quest.expandLabel] as Boolean,
                    isHidden(currentIndex),
                    (jsonObject[Quest.childLabel] as? JsonArray<*>)?.size ?: 0))
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

    internal val onItemClickListener = AdapterView.OnItemClickListener { parentView: AdapterView<*>?, _:
    View?, position: Int, _: Long ->
        val countBefore = count
        MainActivity.loadQuestJson(this.context)

        val quest = getItem(position)
        MainActivity.getNestedArray(quest.index)[Quest.expandLabel] = !(quest.expanded)

        MainActivity.saveJson(this.context)
        notifyDataSetChanged()

        (parentView as ListView).smoothScrollToPosition(position + max(0, count - countBefore))
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val currentQuest = getItem(position)

        val container: View = convertView ?:
            (this.context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.element_header, parent, false)

        val indexPadding =
            context.resources.getDimension(R.dimen.arrow_x_margins).toInt() +
            indentSize * context.resources.displayMetrics.density * (currentQuest.index.size-1)
        container.setPadding(indexPadding.toInt(),
            container.paddingTop, container.paddingRight, container.paddingBottom)

        val questView = container.findViewById(R.id.element_header_text) as TextView

        if(currentQuest.hidden) {
            container.visibility = View.GONE
        } else{
            container.visibility = View.VISIBLE
        }

        val expArrow = container.findViewById(R.id.element_header_arrow)
        if(currentQuest.children > 0) {
            expArrow.visibility = View.VISIBLE
            if (currentQuest.expanded) {
                expArrow.rotation = 90f
            } else {
                expArrow.rotation = 0f
            }
        } else {
            expArrow.visibility = View.INVISIBLE
        }

        questView.text = currentQuest.name

        QuestOptionsDialogFragment.setAddButton(
            this,
            container.findViewById(R.id.element_header_add),
            currentQuest.index,
            {i -> (parent as ListView).smoothScrollToPosition(i)}
        )

        QuestOptionsDialogFragment.setEditButton(
                this, container.findViewById(R.id.element_header_edit), questView.text, currentQuest.index)

        QuestOptionsDialogFragment.setDeleteButton(
                this, container.findViewById(R.id.element_header_delete), currentQuest.index)

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

    fun getPosition(name: String): Int = visibleQuests.indexOfFirst {it.name == name}
}