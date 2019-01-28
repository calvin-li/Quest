package com.sandbox.calvin_li.quest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ListView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.*
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.FileProvider
import android.graphics.drawable.GradientDrawable

class MainActivity : AppCompatActivity() {
    private lateinit var questView: ListView

    internal companion object {
        private const val questFileName: String = "quests.json"
        lateinit var questJson: JsonArray<JsonObject>

        fun saveJson(context: Context) {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED){
                val externalFile = File(context.getExternalFilesDir(null), questFileName)
                if (!externalFile.createNewFile()) { Log.e("Quest error", "File not created") }

                val externalWriteStream = FileOutputStream(externalFile)
                externalWriteStream.write(questJson.toJsonString(true).toByteArray())
                externalWriteStream.close()
            }
        }

        internal fun loadQuestJson(context: Context){
            val questStream: InputStream = try {
                val externalFile = File(context.getExternalFilesDir(null), questFileName)
                FileInputStream(externalFile)
            } catch (ex: IOException) {
                context.resources.openRawResource(R.raw.quests)
            }

            @Suppress("UNCHECKED_CAST")
            questJson = Parser().parse(questStream) as JsonArray<JsonObject>
            questStream.close()
        }

        fun getNestedArray(indices: List<Int>): JsonObject {
            var nestedObject: JsonObject = questJson[indices[0]]

            for (i in 1 until indices.size) {
                @Suppress("UNCHECKED_CAST")
                nestedObject = (nestedObject[Quest.childLabel] as JsonArray<JsonObject>)[indices[i]]
            }
            return nestedObject
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.action_add){
            val editView = QuestOptionsDialogFragment.getDialogView(this)
            editView.hint = "Add new subquest here"

            val dialog = QuestOptionsDialogFragment.createDialog(this, editView, "Add subquest") { _, _ ->
                loadQuestJson(this)

                val newObject = JsonObject()
                newObject[Quest.nameLabel] = editView.text.toString()
                newObject[Quest.expandLabel] = true

                questJson.add(newObject)
                saveJson(this)
                this.onResume()
            }

            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
            dialog.show()
            editView.requestFocus()
        }
        else if(item.itemId == R.id.action_json){
            saveJson(this)
            val jsonFile = File(this.getExternalFilesDir(null), questFileName)

            val jsonUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    jsonFile)

            val jsonIntent = Intent(Intent.ACTION_VIEW)
            jsonIntent.setDataAndType(jsonUri, "text/plain")
            jsonIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            jsonIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivity(Intent.createChooser(jsonIntent, "Open with: "))
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val questsChannel = NotificationChannel(
            NotificationActionReceiver.channelId,
            NotificationActionReceiver.channelId,
            NotificationManager.IMPORTANCE_LOW)
        questsChannel.enableLights(false)
        questsChannel.enableVibration(false)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            questsChannel)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }

    override fun onResume() {
        super.onResume()
        loadQuestJson(this)
        saveJson(this)

        NotificationActionReceiver.createOverallNotification(this)
        val notificationIndexList: MutableList<List<QuestState>> = mutableListOf()
        (0 until questJson.size).forEach { notificationIndexList.add(listOf(QuestState(it, 0))) }
        NotificationActionReceiver.saveIndexList(this, notificationIndexList)
        NotificationActionReceiver.refreshNotifications(this)

        questView = findViewById(R.id.top_view)
        questView.isSmoothScrollbarEnabled = true

        val colors = intArrayOf(
                Color.WHITE,
                getColor(R.color.groovy),
                getColor(R.color.groovy),
                Color.WHITE)
        questView.divider = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        questView.dividerHeight = resources.getDimension(R.dimen.custom_list_divider_height).toInt()

        val adapter = CustomListAdapter(this)
        questView.adapter = adapter
        questView.onItemClickListener = adapter.onItemClickListener
    }
}
