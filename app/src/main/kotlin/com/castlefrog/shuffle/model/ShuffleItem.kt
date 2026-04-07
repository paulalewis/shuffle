package com.castlefrog.shuffle.model

data class ShuffleItem(
    val text: String,
    val group: ShuffleGroup = ShuffleGroup(),
)