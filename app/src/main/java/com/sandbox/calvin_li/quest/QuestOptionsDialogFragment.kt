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

class QuestOptionsDialogFragment : DialogFragment() {
    companion object {
        fun setDeleteButton(adapter: ExpandableListAdapter, deleteButton: Button, index:
        List<Int>) {
            deleteButton.setOnClickListener {
                deleteQuest(index, adapter.context)
                adapter.notifyDataSetChanged()
            }
        }

        fun setEditButton(adapter: ExpandableListAdapter, editButton: Button, currentQuest:
        CharSequence, index: List<Int>) {
            editButton.setOnClickListener {
                val editView = getDialogView(adapter.context)
                editView.append(currentQuest)
                val builder = createBuilder(adapter.context, editView, "Edit quest", { _, _ ->
                    editQuest(index, editView.text.toString(), adapter.context)
                    adapter.notifyDataSetChanged()
                })
                builder.show()
            }
        }

        fun setAddButton(adapter: ExpandableListAdapter, button: Button, index: List<Int>) {
            button.setOnClickListener {
                val editView = getDialogView(adapter.context)
                editView.hint = "Add new subquest here"
                val builder = createBuilder(adapter.context, editView, "Add subquest", { _, _ ->
                    addSubQuest(index, editView.text.toString(), adapter.context)
                    adapter.notifyDataSetChanged()
                })
                builder.show()
            }
        }

        fun deleteQuest(indices: List<Int>, context: Context) {
            val leafIndex = indices.last()
            MainActivity.loadQuestJson(context)

            var toDelete: JsonArray<JsonObject> = MainActivity.questJson
            @Suppress("UNCHECKED_CAST")
            if (indices.size > 1) {
                toDelete =
                    MainActivity.getNestedArray(indices.dropLast(1))[MultiLevelListView.childLabel]
                        as JsonArray<JsonObject>
            }
            toDelete.removeAt(leafIndex)
            MainActivity.saveJson(context)

            if(indices.size == 1){
                NotificationActionReceiver.removeAndShiftNotification(context, indices.first())
            } else{
                val notificationIndices = NotificationActionReceiver.getIndexList(context)
                notificationIndices[indices.first()] = indices.dropLast(1)
                NotificationActionReceiver.saveIndexList(context, notificationIndices)
            }
            NotificationActionReceiver.refreshNotifications(context)
        }

        fun editQuest(index: List<Int>, text: String, context: Context) {
            MainActivity.loadQuestJson(context)
            val nestedObject: JsonObject = MainActivity.getNestedArray(index)
            nestedObject[MultiLevelListView.nameLabel] = text
            MainActivity.saveJson(context)
            NotificationActionReceiver.refreshNotifications(context)
        }

        fun addSubQuest(index: List<Int>, text: String, context: Context) {
            MainActivity.loadQuestJson(context)
            val currentObject = MainActivity.getNestedArray(index)
            val childObject: JsonArray<JsonObject>? =
                currentObject[MultiLevelListView.childLabel] as JsonArray<JsonObject>?
            val newObject = JsonObject()
            newObject.put(MultiLevelListView.nameLabel, text)
            if (childObject == null) {
                currentObject.put(MultiLevelListView.childLabel, JsonArray(newObject))
            } else {
                childObject.add(newObject)
            }
            MainActivity.saveJson(context)
            NotificationActionReceiver.refreshNotifications(context)
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