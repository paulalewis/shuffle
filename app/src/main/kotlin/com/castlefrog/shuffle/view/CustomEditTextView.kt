package com.castlefrog.shuffle.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomEditTextView(
    label: String = "",
    value: TextFieldValue,
    errorLabel: String = "",
    onValueChange: (TextFieldValue) -> Unit = {}
) {
    Column {
        if (label.isNotEmpty()) {
            Text(
                text = label
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (errorLabel.isNotEmpty()) {
            Text(
                color = Color.Red,
                text = errorLabel
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            // border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary)
        ) {
            TextField(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                value = value,
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    // color = MaterialTheme.colorScheme.onSecondary,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                onValueChange = onValueChange
            )
        }
    }
}

@Preview
@Composable
fun CustomEditTextViewPreview() {
    Box(
        modifier = Modifier.background(color = Color.White)
    ) {
        CustomEditTextView(
            label = "Author",
            value = TextFieldValue("Isaac Asimov"),
        )
    }
}

@Preview
@Composable
fun CustomEditTextViewErrorPreview() {
    Box(
        modifier = Modifier.background(color = Color.White)
    ) {
        CustomEditTextView(
            label = "Author",
            value = TextFieldValue(""),
            errorLabel = "Name must not be empty"
        )
    }
}
