package com.sandbox.calvin_li.quest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ExpandableListView
import com.sandbox.calvin_li.quest.ExpandableListAdapter.ExpandableListAdapter

class MainActivity : AppCompatActivity() {
    lateinit var listAdapter: ExpandableListAdapter
    lateinit var expandListView: ExpandableListView
    lateinit var listDataHeader: List<String>
    lateinit var listDataChild: HashMap<String, List<Pair<String, Any?>>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expandListView = findViewById(R.id.topView) as ExpandableListView
        prepareListData()
        listAdapter = ExpandableListAdapter(this, listDataHeader, listDataChild)
        expandListView.setAdapter(listAdapter)
    }

    private fun prepareListData() {
        listDataHeader = listOf("Top 250", "Now Showing", "Coming Soon")
        listDataChild = HashMap<String, List<Pair<String, Any?>>>()

        // Adding child data
        val top250 = listOf(
                "The Shawshank Redemption",
                "The Godfather",
                "The Godfather: Part II",
                "Pulp Fiction",
                "The Good, the Bad, the Ugly",
                "The Dark Knight",
                "12 Angry Men")

        val nowShowing = listOf(
                "The Conjuring",
                "Despicable Me 2",
                "Turbo",
                "Grown Ups 2",
                "Red 2",
                "The Wolverine")

        val comingSoon = listOf(
                "2 Guns",
                "The Smurfs 2",
                "The Spectacular Now",
                "The Canyons",
                "Europa Report")

        listDataChild.put(listDataHeader[0], cap(top250))
        listDataChild.put(listDataHeader[1], cap(nowShowing))
        listDataChild.put(listDataHeader[2], cap(comingSoon))
    }

    private fun cap(list: List<String>): List<Pair<String, Any?>>{
        return list.zip(List(list.size, {null}))
    }
}
