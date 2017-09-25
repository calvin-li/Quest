package com.sandbox.calvin_li.quest

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

class ExpandableListAdapter(
        internal val context: Context,
        private val parent: ExpandableListAdapter?,
        private val leafIndex: Int?,
        private val index: List<Int>)
    : BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): JsonObject{
        if(index.isEmpty()){
            return MainActivity.questJson[groupPosition]
        } else{
            return MainActivity.getNestedArray(index)[leafIndex!!]
        }
    }

    override fun getGroupCount(): Int {
        if(index.isEmpty()){
            return MainActivity.questJson.size
        } else{
            return 1
        }
    }

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getGroupView(
            groupPosition: Int,
            isExpanded: Boolean,
            convertView: View?,
            parent: ViewGroup
    ): View? {
        val returnedView: View = convertView ?: (this.context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.element_header, null)

        val labelListHeader = returnedView.findViewById(R.id.element_header_text) as TextView
        labelListHeader.setTypeface(null, Typeface.BOLD)

        labelListHeader.tag = groupPosition
        labelListHeader.text = getGroup(groupPosition)[MultiLevelListView.nameLabel] as String

        val editButton = returnedView.findViewById(R.id.element_header_edit) as Button
        QuestOptionsDialogFragment.setEditButton(this, editButton, labelListHeader.text, index,
                leafIndex?:groupPosition)

        val deleteButton = returnedView.findViewById(R.id.element_header_delete) as Button
        QuestOptionsDialogFragment.setDeleteButton(this.parent?: this, deleteButton, index,
                leafIndex?:groupPosition)

        val addQuestButton = returnedView.findViewById(R.id.element_header_add) as Button
        QuestOptionsDialogFragment.setAddButton(this, addQuestButton, index, leafIndex?:groupPosition)

        return returnedView
    }

    override fun getChild(groupPosition: Int, childPosition: Int): JsonObject {
        val group: JsonObject = this.getGroup(groupPosition)
        return (group[MultiLevelListView.childLabel] as JsonArray<JsonObject>)[childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup)
            : View {
        val elementBody = (this.context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.element_body, null)
        val childView: MultiLevelListView = convertView as? MultiLevelListView ?:
                elementBody.findViewById(R.id.element_children) as MultiLevelListView

        val childAdapter = ExpandableListAdapter(
                context, this, childPosition, index.plusElement(leafIndex?:groupPosition))

        childView.setAdapter(childAdapter)
        return childView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val child: Any? = this.getGroup(groupPosition)[MultiLevelListView.childLabel]
        // If child is not present, `getGroup(groupPosition)[childLabel]` is null, which is is bubbled
        // to the `?:` operator.
        return (child as? JsonArray<JsonObject>)?.size ?: 0
    }

    override fun hasStableIds(): Boolean = false

    override fun isChildSelectable(i: Int, i1: Int): Boolean = true
}
