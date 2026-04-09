package com.castlefrog.shuffle.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.castlefrog.shuffle.model.ShuffleItem

@Composable
fun CardView(
    id: Int,
    onSelectItemListener: (id: Int) -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Card(
        modifier = Modifier.padding(Dimens.CORNER_RADIUS.dp),
        shape = RoundedCornerShape(Dimens.INNER_PADDING.dp),
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = {
                        onSelectItemListener.invoke(id)
                    },
                ),
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun CardViewPreview() {
    CardView(1) {
        ItemView(ShuffleItem("squats"))
    }
}
