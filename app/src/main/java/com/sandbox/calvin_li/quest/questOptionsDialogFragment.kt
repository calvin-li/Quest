package com.sandbox.calvin_li.quest

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.widget.Button

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
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setMessage("")
            return builder.create()
    }
}