package com.sandbox.calvin_li.quest.ExpandableListAdapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.sandbox.calvin_li.quest.R

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
        val child = getChild(groupPosition, childPosition) as Pair<*, *>

        val childText: String = child.first as String

        val newConvertView: View = convertView ?: (this._context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.element, null)

        val textListChild: TextView =
                (convertView ?: newConvertView).findViewById(R.id.elementText) as TextView

        textListChild.text = childText
        return convertView ?: newConvertView
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
                .LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.element, null)

        val labelListHeader: TextView = returnedView.findViewById(R.id.elementText) as TextView
        labelListHeader.setTypeface(null, Typeface.BOLD)
        labelListHeader.text = getGroup(groupPosition) as String

        return returnedView
    }

    override fun hasStableIds(): Boolean = false

    override fun isChildSelectable(i: Int, i1: Int): Boolean = true
}
