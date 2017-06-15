package com.sandbox.calvin_li.quest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.sandbox.calvin_li.quest.ExpandableListAdapter.ExpandableListAdapter
import com.sandbox.calvin_li.quest.MultiLevelListView.MultiLevelListView

class TestClickListener: AdapterView.OnItemClickListener{
    override fun onItemClick(p0: AdapterView<*>?, p1: View, p2: Int, p3: Long) {
        System.out.println(p1.tag)
    }
}

class MainActivity : AppCompatActivity() {
    lateinit var listAdapter: ExpandableListAdapter
    lateinit var expandListView: MultiLevelListView
    lateinit var listDataHeader: List<String>
    lateinit var listDataChild: HashMap<String, List<Pair<String, Any?>>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expandListView = findViewById(R.id.top_view) as MultiLevelListView
        prepareListData()
        listAdapter = ExpandableListAdapter(this, listDataHeader, listDataChild)

        expandListView.onItemClickListener = TestClickListener()
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

    private fun cap(list: List<String>): List<Pair<String, Any?>> {
        val level3 = listOf("sub1", "sub2", "sub3")
        fun emptyHash(name: String): HashMap<String, List<Pair<String, Any?>>> =
                if (name == "Europa Report") {
                    hashMapOf(name to cap(level3))
                } else {
                    hashMapOf(name to emptyList<Pair<String, Any?>>())
                }
        return list.map { Pair(it, emptyHash(it)) }
    }
}
