package com.sandbox.calvin_li.quest

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.EditText
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView
import java.io.FileOutputStream

class ExpandableListAdapter(
        private val _context: Context,
        private val _questSubList: JsonArray<JsonObject>)
    : BaseExpandableListAdapter() {

    private companion object {
        val nameLabel: String = "name"
        val childLabel: String = "child"
    }

    override fun getGroup(groupPosition: Int): JsonObject =
            this._questSubList[groupPosition]

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

        val labelListHeader = returnedView.findViewById(R.id.element_header_text) as EditText
        labelListHeader.setTypeface(null, Typeface.BOLD)

        labelListHeader.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(sequence: Editable?) {}
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                getGroup(labelListHeader.tag as Int)[nameLabel] = sequence.toString()
                saveJson(_questSubList)
            }
        })

        labelListHeader.tag = groupPosition
        labelListHeader.setText(getGroup(groupPosition)[nameLabel] as String)

        return returnedView
    }

    override fun getChild(groupPosition: Int, childPosition: Int): JsonObject {
        val group: JsonObject = this.getGroup(groupPosition)
        return (group[childLabel] as JsonArray<JsonObject>)[childPosition]
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
        val childView: MultiLevelListView = convertView as? MultiLevelListView ?:
                elementBody.findViewById(R.id.element_children) as MultiLevelListView

        val child: JsonObject = getChild(groupPosition, childPosition)

        val childAdapter = ExpandableListAdapter(_context, JsonArray(child))
        childView.setAdapter(childAdapter)

        return childView
    }

    override fun getChildrenCount(groupPosition: Int): Int =
            // If child is not present, `getGroup(groupPosition)[childLabel]` is null,
            // which is is bubbled to the `?:` operator.
            (this.getGroup(groupPosition)[childLabel] as? JsonArray<JsonObject>)?.size ?: 0

    override fun hasStableIds(): Boolean = false

    override fun isChildSelectable(i: Int, i1: Int): Boolean = true

    fun saveJson(json: JsonArray<JsonObject>){
        val writeStream: FileOutputStream = _context.openFileOutput(MainActivity.questFileName, Context
                .MODE_PRIVATE)
        writeStream.write(json.toJsonString().toByteArray())
        writeStream.close()
    }
}
