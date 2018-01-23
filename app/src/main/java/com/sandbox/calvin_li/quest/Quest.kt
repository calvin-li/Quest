package com.sandbox.calvin_li.quest

class Quest(
    internal val name: String,
    internal val index: List<Int>,
    internal val expanded: Boolean
){
    internal companion object {
        internal const val nameLabel: String = "name"
        internal const val expandLabel: String = "expanded"
        internal const val childLabel: String = "child"
    }
}