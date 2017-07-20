package com.sandbox.calvin_li.quest

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class questOptionsDialogFragment: DialogFragment() {
    companion object {
        fun setDeleteButton(deleteButton: Button, context: Context, index: List<Int>) {
            deleteButton.setOnClickListener {
                Toast.makeText(context, "delete pressed!", Toast.LENGTH_SHORT).show()
                MainActivity.deleteQuest(index)
                MainActivity.saveJson(context)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setMessage("")
            return builder.create()
    }
}