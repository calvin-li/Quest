import android.content.Context
import android.widget.ExpandableListView

class MultiLevelListView(context: Context,  groupPosition: Int, childPosition: Int, groupId: Int) :
        ExpandableListView(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(960, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(600, MeasureSpec.AT_MOST))
}