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

class questOptionsDialogFragment: DialogFragment() {
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
        CharSequence) {
            editButton.setOnClickListener {
                val layoutInflater: LayoutInflater = adapter.context.getSystemService(Context
                        .LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val editView = layoutInflater.inflate(R.layout.element_dialog, null) as EditText
                editView.append(currentQuest)
                val builder = AlertDialog.Builder(adapter.context)
                builder.setTitle("Edit quest")
                        .setView(editView)
                        .setPositiveButton("Confirm", DialogInterface.OnClickListener { dialogInterface, i -> })
                        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i -> })
                builder.show()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setMessage("")
            return builder.create()
    }
}