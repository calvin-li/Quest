package com.sandbox.calvin_li.quest

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

class QuestOptionsDialogFragment : DialogFragment() {
    companion object {
        fun setAddButton(adapter: ArrayAdapter<*>, button: Button, index: List<Int>) {
            button.setOnClickListener {
                val editView = getDialogView(adapter.context)
                editView.hint = "Add new subquest here"

                val dialog = createDialog(adapter.context, editView, "Add subquest", { _, _ ->
                    addSubQuest(index, editView.text.toString(), adapter.context)
                    adapter.notifyDataSetChanged()
                })
                dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
                dialog.show()
            }
        }

        fun setEditButton(adapter: ArrayAdapter<*>, editButton: Button, currentQuest:
        CharSequence, index: List<Int>) {
            editButton.setOnClickListener {
                val editView = getDialogView(adapter.context)
                editView.append(currentQuest)

                val dialog = createDialog(adapter.context, editView, "Edit quest", { _, _ ->
                    editQuest(index, editView.text.toString(), adapter.context)
                    adapter.notifyDataSetChanged()
                })
                dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
                dialog.show()
            }
        }

        fun setDeleteButton(adapter: ArrayAdapter<*>, deleteButton: Button, index:
        List<Int>) {
            deleteButton.setOnClickListener {
                deleteQuest(index, adapter.context)
                adapter.notifyDataSetChanged()
            }
        }

        fun addSubQuest(index: List<Int>, text: String, context: Context) {
            MainActivity.loadQuestJson(context)
            val currentObject = MainActivity.getNestedArray(index)
            currentObject[Quest.expandLabel] = true

            val newObject = JsonObject()
            newObject[Quest.nameLabel] = text
            newObject[Quest.expandLabel] = true

            @Suppress("UNCHECKED_CAST")
            val childObject: JsonArray<JsonObject>? =
                    currentObject[Quest.childLabel] as JsonArray<JsonObject>?
            if (childObject == null) {
                currentObject[Quest.childLabel] = JsonArray(newObject)
            } else {
                childObject.add(newObject)
            }

            MainActivity.saveJson(context)
            NotificationActionReceiver.refreshNotifications(context)
        }

        fun editQuest(index: List<Int>, text: String, context: Context) {
            MainActivity.loadQuestJson(context)
            val nestedObject: JsonObject = MainActivity.getNestedArray(index)
            nestedObject[Quest.nameLabel] = text
            MainActivity.saveJson(context)
            NotificationActionReceiver.refreshNotifications(context)
        }

        fun deleteQuest(indices: List<Int>, context: Context) {
            val leafIndex = indices.last()
            MainActivity.loadQuestJson(context)

            var toDelete: JsonArray<JsonObject> = MainActivity.questJson
            @Suppress("UNCHECKED_CAST")
            if (indices.size > 1) {
                toDelete =
                    MainActivity.getNestedArray(indices.dropLast(1))[Quest.childLabel]
                        as JsonArray<JsonObject>
            }
            toDelete.removeAt(leafIndex)
            MainActivity.saveJson(context)

            if(indices.size == 1){
                NotificationActionReceiver.removeAndShiftNotification(context, indices.first())
            } else{
                val notificationIndices = NotificationActionReceiver.getIndexList(context)
                notificationIndices[indices.first()] =
                    notificationIndices[indices.first()].take(indices.size-1)
                NotificationActionReceiver.saveIndexList(context, notificationIndices)
            }
            NotificationActionReceiver.refreshNotifications(context)
        }

        internal fun createDialog(context: Context, view: EditText, title: String,
                                  positiveAction: (DialogInterface, Int) -> Unit):
                AlertDialog {
            return AlertDialog.Builder(context).setTitle(title)
                    .setView(view)
                    .setPositiveButton("Confirm", positiveAction)
                    .setNegativeButton("Cancel", { _, _ -> })
                    .create()
        }

        internal fun getDialogView(context: Context): EditText {
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