package com.sandbox.calvin_li.quest

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView

class questOptionsDialogFragment : DialogFragment() {
    companion object {
        fun setDeleteButton(parentAdapter: ExpandableListAdapter, deleteButton: Button, index:
        List<Int>, leafIndex: Int) {
            deleteButton.setOnClickListener {
                val toDelete = MainActivity.getNestedArray(index)
                toDelete.removeAt(leafIndex)
                parentAdapter.notifyDataSetChanged()
                MainActivity.saveJson(parentAdapter.context)
            }
        }

        fun setEditButton(adapter: ExpandableListAdapter, editButton: Button, currentQuest:
        CharSequence, index: List<Int>, leafIndex: Int) {
            editButton.setOnClickListener {
                val editView = getDialogView(adapter.context)
                editView.append(currentQuest)
                val builder = createBuilder(adapter.context, editView, "Edit quest", {
                    _, _ ->
                    val nestedArray = MainActivity.getNestedArray(index)
                    nestedArray[leafIndex][MultiLevelListView.nameLabel] = editView.text.toString()
                    MainActivity.saveJson(adapter.context)
                })
                builder.show()
            }
        }

        fun setAddButton(adapter: ExpandableListAdapter, button: Button, index: List<Int>,
                         leafIndex: Int) {
            button.setOnClickListener {
                val editView = getDialogView(adapter.context)
                editView.hint = "Add new subquest here"
                val builder = createBuilder(adapter.context, editView, "Add subquest", {
                    _, _ ->
                    val currentObject = MainActivity.getNestedArray(index)[leafIndex]
                    val childObject: JsonArray<JsonObject>? =
                            currentObject[MultiLevelListView.childLabel] as JsonArray<JsonObject>?
                    val newObject = JsonObject()
                    newObject.put(MultiLevelListView.nameLabel, editView.text.toString())
                    if (childObject == null) {
                        currentObject.put(MultiLevelListView.childLabel, JsonArray(newObject))
                    } else {
                        childObject.add(newObject)
                    }
                    MainActivity.saveJson(adapter.context)
                })
                builder.show()
            }
        }

        private fun  createBuilder(context: Context, view: EditText, title: String,
                                   positiveAction: (DialogInterface, Int) -> Unit):
                AlertDialog.Builder {
            return AlertDialog.Builder(context).setTitle(title)
                    .setView(view)
                    .setPositiveButton("Confirm", positiveAction)
                    .setNegativeButton("Cancel", { _, _ -> })
        }

        private fun getDialogView(context: Context): EditText {
            val layoutInflater: LayoutInflater = context.getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return layoutInflater.inflate(R.layout.element_dialog, null) as EditText
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setMessage("")
            return builder.create()
    }
}