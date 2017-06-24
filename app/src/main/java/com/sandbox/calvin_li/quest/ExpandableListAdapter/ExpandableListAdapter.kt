package com.sandbox.calvin_li.quest.ExpandableListAdapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.beust.klaxon.JsonObject
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView
import com.sandbox.calvin_li.quest.R
import org.json.JSONObject

class ExpandableListAdapter(
        private val _context: Context,
        private val _questSubList: JsonObject)
    : BaseExpandableListAdapter() {

    override fun getChild(groupPosition: Int, childPosition: Int): Any? {
        return this.getGroup(groupPosition) as JSONObject
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
        val elementBody = (this._context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.element_body, null)
        val child: Pair<*, *> = getChild(groupPosition, childPosition) as Pair<*, *>

        val childView: MultiLevelListView = convertView as? MultiLevelListView ?:
                elementBody.findViewById(R.id.element_children) as MultiLevelListView

        val childList = child.second as HashMap<String, List<Pair<String, Any?>>>

        val childAdapter = ExpandableListAdapter(
                _context,
                this.getGroup(groupPosition) as JsonObject)

        childView.setAdapter(childAdapter)

        return childView
    }

    override fun getChildrenCount(groupPosition: Int): Int =
            (this.getGroup(groupPosition) as JSONObject).length()

    override fun getGroup(groupPosition: Int): Any? =
            this._questSubList.values.toTypedArray()[groupPosition]

    override fun getGroupCount(): Int = this._questSubList.size

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getGroupView(
            groupPosition: Int,
            isExpanded: Boolean,
            convertView: View?,
            parent: ViewGroup
    ): View? {
        val returnedView: View = convertView ?: (this._context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.element_header, null)

        val labelListHeader: TextView = returnedView.findViewById(R.id.element_header_text) as TextView
        labelListHeader.setTypeface(null, Typeface.BOLD)
        labelListHeader.text = getGroup(groupPosition) as String

        return returnedView
    }

    override fun hasStableIds(): Boolean = false

    override fun isChildSelectable(i: Int, i1: Int): Boolean = true
}
