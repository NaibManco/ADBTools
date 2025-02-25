package utils

data class FragmentInfo(
    val name: String,
    val id: String,
    val children: List<FragmentInfo>,
    var tag: String? = null,
    var state: String = "",
    var isAdded: Boolean = true,
    var isVisible: Boolean = true,
    var indent: Int = 0
) 