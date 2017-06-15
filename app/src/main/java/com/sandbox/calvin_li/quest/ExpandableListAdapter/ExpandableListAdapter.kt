package com.sandbox.calvin_li.quest.ExpandableListAdapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import com.sandbox.calvin_li.quest.R
import com.sandbox.calvin_li.quest.TestClickListener
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView

import kotlin.collections.HashMap

class ExpandableListAdapter(
        private val _context: Context,
        private val _listDataHeader: List<String>,
        private val _listDataChild: HashMap<String, List<Pair<String, Any?>>>)
    : BaseExpandableListAdapter() {

    override fun getChild(groupPosition: Int, childPosition: Int): Any? {
        return this._listDataChild[this._listDataHeader[groupPosition]]?.get(childPosition)
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
                listOf(child.first as String),
                childList)

        childView.onItemClickListener = TestClickListener()
        childView.setAdapter(childAdapter)

        return childView
    }

    override fun getChildrenCount(groupPosition: Int): Int =
            this._listDataChild[this._listDataHeader[groupPosition]]?.size as Int

    override fun getGroup(groupPosition: Int): Any? = this._listDataHeader[groupPosition]

    override fun getGroupCount(): Int = this._listDataHeader.size

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
