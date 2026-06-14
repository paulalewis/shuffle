package com.castlefrog.shuffle.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.castlefrog.shuffle.model.ShuffleItem

@Composable
fun ItemView(
    item: ShuffleItem,
) {
    Column(
        modifier = Modifier.padding(Dimens.INNER_PADDING.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.text,
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Start
            ),
        )
    }
}

@Preview
@Composable
fun ItemViewPreview() {
    ItemView(
        item = ShuffleItem(text = "Swimming")
    )
}
