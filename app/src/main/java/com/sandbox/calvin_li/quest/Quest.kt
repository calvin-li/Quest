package com.sandbox.calvin_li.quest

class Quest(
    internal val name: String,
    internal val index: List<Int>,
    internal val expanded: Boolean,
    internal val hidden: Boolean,
    internal val children: Int,
    internal val checked: Boolean
){
    internal companion object {
        internal const val NAME_LABEL: String = "name"
        internal const val EXPAND_LABEL: String = "expanded"
        internal const val CHILD_LABEL: String = "child"
        internal const val CHECKED_LABEL: String = "checked"
    }
}