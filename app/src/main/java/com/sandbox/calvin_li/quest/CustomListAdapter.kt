package com.sandbox.calvin_li.quest

import android.annotation.SuppressLint
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
: ArrayAdapter<Quest>(context, 0, quests) {     // Resource parameter is not used
    internal companion object {
        private const val indentSize = 24 // in dp

        private fun flatten(questJson: JsonArray<JsonObject>?, currentIndex: List<Int>)
                : List<Quest> {
            return questJson?.mapIndexed { index, jsonObject ->
                listOf(
                    Quest(
                        jsonObject[Quest.nameLabel] as String,
                        currentIndex.plus(index),
                        jsonObject[Quest.expandLabel] as Boolean,
                        isHidden(currentIndex),
                        (jsonObject[Quest.childLabel] as? JsonArray<*>)?.size ?: 0,
                        jsonObject[Quest.checkedLabel] as? Boolean ?: false))
                    .plus(
                        @Suppress("UNCHECKED_CAST")
                        flatten(
                        jsonObject[Quest.childLabel] as? JsonArray<JsonObject>,
                        currentIndex.plus(index)))
            }?.flatten() ?: emptyList()
        }

        private fun isHidden(index: List<Int>) =
            (index.size - 1 downTo 0).toList()
                .mapIndexed{ it, _ -> index.subList(0, it+1) }
                .any { !(MainActivity.getNestedArray(it)[Quest.expandLabel] as Boolean) }
    }

    private var animateCheckBoxes: Boolean = false

    private fun toggleCheckQuest(index: List<Int>){
        val parentJson = if (index.size > 1) {
            MainActivity.getNestedArray(index.dropLast(1))
        } else {
            null
        }
        val questJson: JsonObject = if (parentJson == null) {
            MainActivity.getNestedArray(index)
        } else {
            (parentJson[Quest.childLabel] as JsonArray<*>)[index.last()] as JsonObject
        }

        val isChecked: Boolean = questJson[Quest.checkedLabel] as? Boolean ?: false
        setCheck(questJson, !isChecked)

        if (parentJson != null){
            parentJson[Quest.checkedLabel] =
                (parentJson[Quest.childLabel] as JsonArray<*>).all {
                    (it as JsonObject)[Quest.checkedLabel] as Boolean
                }
        }

        MainActivity.saveJson(context)
        notifyDataSetChanged(true)
    }

    private fun setCheck(quest: JsonObject, checked: Boolean) {
        quest[Quest.checkedLabel] = checked

        (quest[Quest.childLabel] as? JsonArray<*>?)?.forEach {
            setCheck(it as JsonObject, checked)
        }
    }

    private fun visibleQuests() = quests.filter { !it.hidden }

    internal val onItemClickListener = AdapterView.OnItemClickListener { parentView: AdapterView<*>?, _:
    View?, position: Int, _: Long ->
        val countBefore = count
        MainActivity.loadQuestJson(this.context)

        val quest = getItem(position)
        MainActivity.getNestedArray(quest.index)[Quest.expandLabel] = !(quest.expanded)

        MainActivity.saveJson(this.context)
        notifyDataSetChanged(false)

        (parentView as ListView).smoothScrollToPosition(position + max(0, count - countBefore))
    }

    @SuppressLint("SetTextI18n")
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

        if(currentQuest.hidden) {
            container.visibility = View.GONE
        } else{
            container.visibility = View.VISIBLE
        }

        val expArrow: TextView = container.findViewById(R.id.element_header_arrow)
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

        val checkBox = container.findViewById<CheckBox>(R.id.element_header_checkbox)
        checkBox.setOnClickListener { toggleCheckQuest(currentQuest.index) }

        checkBox.isChecked = currentQuest.checked
        if (!animateCheckBoxes) {
            checkBox.jumpDrawablesToCurrentState()
        }

        val questView = container.findViewById(R.id.element_header_text) as TextView
        questView.text = currentQuest.name

        val numChildrenView = container.findViewById(R.id.element_header_children) as TextView
        if(currentQuest.children > 0) {
            numChildrenView.text = "+${currentQuest.children}"
        } else{
            numChildrenView.text = ""
        }

        QuestOptionsDialogFragment.setAddButton(
            this,
            container.findViewById(R.id.element_header_add),
            currentQuest.index
        ) {(parent as ListView).smoothScrollToPosition(getPosition(currentQuest.name))}

        QuestOptionsDialogFragment.setEditButton(
                this, container.findViewById(R.id.element_header_edit), questView.text, currentQuest.index)

        QuestOptionsDialogFragment.setDeleteButton(
                this, container.findViewById(R.id.element_header_delete), currentQuest.index)

        return container
    }

    fun notifyDataSetChanged(animate: Boolean) {
        MainActivity.loadQuestJson(this.context)
        quests = flatten(MainActivity.questJson, emptyList()).toTypedArray()

        animateCheckBoxes = animate
        super.notifyDataSetChanged()
    }

    override fun getCount(): Int = visibleQuests().count()

    override fun getItem(position: Int): Quest = visibleQuests()[position]

    override fun getItemId(position: Int): Long {
        val currentQuest = getItem(position)
        return currentQuest.name.hashCode().toLong() * currentQuest.index.hashCode()
        //implicit cast right operand
    }

    override fun getPosition(item: Quest?): Int = visibleQuests().indexOfFirst {it == item}

    private fun getPosition(name: String): Int = visibleQuests().indexOfFirst {it.name == name}
}