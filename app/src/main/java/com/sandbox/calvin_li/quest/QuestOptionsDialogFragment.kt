package com.sandbox.calvin_li.quest

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import androidx.fragment.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

class QuestOptionsDialogFragment : DialogFragment() {
    companion object {
        fun setAddButton(
            adapter: CustomListAdapter, clickable: View, index: List<Int>, scroll: () -> Unit) {
            clickable.setOnClickListener {
                val editView = getDialogView(adapter.context)

                val dialog = createDialog(adapter.context, editView, "Add subquest") { _, _ ->
                    val newQuest = editView.text.toString()
                    addSubQuest(index, newQuest, adapter.context)
                    adapter.notifyDataSetChanged(false)
                    scroll()
                }

                editView.setOnEditorActionListener { _, _, _ ->
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).callOnClick()
                }

                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
                dialog.show()
                editView.requestFocus()
            }
        }

        fun setEditButton(adapter: CustomListAdapter, clickable: View, currentQuest:
        CharSequence, index: List<Int>) {
            clickable.setOnClickListener {
                val editView = getDialogView(adapter.context)
                editView.append(currentQuest)
                editView.hint = currentQuest

                val dialog = createDialog(adapter.context, editView, "Edit quest") { _, _ ->
                    editQuest(index, editView.text.toString(), adapter.context)
                    adapter.notifyDataSetChanged(false)
                }

                editView.setOnEditorActionListener { _, _, _ ->
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).callOnClick()
                }

                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
                dialog.show()
                editView.requestFocus()
            }
        }

        fun setDeleteButton(adapter: CustomListAdapter, clickable: View, index:
        List<Int>) {
            clickable.setOnClickListener {
                deleteQuest(index, adapter.context)
                adapter.notifyDataSetChanged(false)
            }
        }

        fun addSubQuest(index: List<Int>, text: String, context: Context) {
            MainActivity.loadQuestJson(context)
            val currentObject = MainActivity.getNestedArray(index)
            currentObject[Quest.expandLabel] = true

            val newObject = JsonObject()
            newObject[Quest.nameLabel] = text
            newObject[Quest.expandLabel] = true
            newObject[Quest.checkedLabel] = false

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

        internal fun createDialog(context: Context, editView: EditText, title: String,
                                  positiveAction: (DialogInterface, Int) -> Unit): AlertDialog {
            if(MainActivity.inNightMode(context)) {
                editView.setTextColor(context.resources.getColor(R.color.light_text, null))
                editView.setHintTextColor(context.resources.getColor(R.color.light_hint_text, null))
            }
            return AlertDialog.Builder(context, R.style.QuestDialogStyle)
                .setTitle(title)
                .setView(editView)
                .setPositiveButton("Confirm", positiveAction)
                .setNegativeButton("Cancel") { _, _ -> }
                .create()
        }

        @SuppressLint("InflateParams")
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