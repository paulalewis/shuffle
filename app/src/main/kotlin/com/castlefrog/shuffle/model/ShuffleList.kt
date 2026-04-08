package com.castlefrog.shuffle.model

data class ShuffleList(
    val name: String,
    val subsetSize: Int,
    val items: List<ShuffleItem>,
)