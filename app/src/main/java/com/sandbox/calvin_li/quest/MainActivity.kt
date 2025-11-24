package com.sandbox.calvin_li.quest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
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
import android.content.res.Configuration
import android.graphics.Color
import androidx.core.content.FileProvider
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    private lateinit var questView: ListView
    private var nightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

    internal companion object {
        private const val QUEST_FILE_NAME: String = "quests.json"

        private const val DAY_NIGHT_MODE: String = "dayNightMode"

        lateinit var questJson: JsonArray<JsonObject>

        fun saveJson(context: Context) {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED){
                val externalFile = File(context.getExternalFilesDir(null), QUEST_FILE_NAME)
                if (!externalFile.createNewFile()) { Log.e("Quest error", "File not created") }

                val externalWriteStream = FileOutputStream(externalFile)
                externalWriteStream.write(questJson.toJsonString(true).toByteArray())
                externalWriteStream.close()
            }
        }

        internal fun loadQuestJson(context: Context){
            val questStream: InputStream = try {
                val externalFile = File(context.getExternalFilesDir(null), QUEST_FILE_NAME)
                FileInputStream(externalFile)
            } catch (_: IOException) {
                context.resources.openRawResource(R.raw.quests)
            }

            @Suppress("UNCHECKED_CAST")
            questJson = Parser.default().parse(questStream) as JsonArray<JsonObject>
            questStream.close()
        }

        internal fun inNightMode(context: Context) =
            context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES


        fun getNestedArray(indices: List<Int>): JsonObject {
            var nestedObject: JsonObject = questJson[indices[0]]

            for (i in 1 until indices.size) {
                @Suppress("UNCHECKED_CAST")
                nestedObject = (nestedObject[Quest.CHILD_LABEL] as JsonArray<JsonObject>)[indices[i]]
            }
            return nestedObject
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add -> {
                val editView = QuestOptionsDialogFragment.getDialogView(this)
                editView.hint = "Add new subquest here"

                val dialog = QuestOptionsDialogFragment.createDialog(this, editView, "Add subquest")
                { _, _ ->
                    loadQuestJson(this)

                    val newObject = JsonObject()
                    newObject[Quest.NAME_LABEL] = editView.text.toString()
                    newObject[Quest.EXPAND_LABEL] = true
                    newObject[Quest.CHECKED_LABEL] = false

                    questJson.add(newObject)
                    saveJson(this)
                    this.onResume()
                }

                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
                dialog.show()
                editView.requestFocus()
            }
            R.id.action_json -> {
                saveJson(this)
                val jsonFile = File(this.getExternalFilesDir(null), QUEST_FILE_NAME)

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
            R.id.night_follow_system -> {
                setDayNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            R.id.night_yes -> {
                setDayNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            R.id.night_no -> {
                setDayNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            R.id.battery_auto -> {
                setDayNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(
            getPreferences(MODE_PRIVATE).getInt(
                DAY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))

        val questsChannel = NotificationChannel(
            NotificationActionReceiver.CHANNEL_ID,
            NotificationActionReceiver.CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT)
        questsChannel.enableLights(false)
        questsChannel.enableVibration(false)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(questsChannel)

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

        Configuration.UI_MODE_NIGHT_MASK
        val bgColor = if (inNightMode()) R.color.primary_dark else Color.WHITE
        val colors = intArrayOf(
                bgColor,
                getColor(R.color.groovy),
                getColor(R.color.groovy),
                bgColor)
        questView.divider = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        questView.dividerHeight = resources.getDimension(R.dimen.custom_list_divider_height).toInt()

        val adapter = CustomListAdapter(this)
        questView.adapter = adapter
        questView.onItemClickListener = adapter.onItemClickListener
    }

    private fun setDayNightMode(newMode: Int) {
        val sharedPrefs = getPreferences(MODE_PRIVATE)
        sharedPrefs.edit {
            nightMode = newMode
            putInt(DAY_NIGHT_MODE, nightMode)
            apply()
        }
        recreate()
    }

    private fun inNightMode() = inNightMode(this)
}
