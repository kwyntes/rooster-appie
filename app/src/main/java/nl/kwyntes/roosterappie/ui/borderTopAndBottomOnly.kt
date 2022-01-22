package nl.kwyntes.roosterappie.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

fun Modifier.borderTopAndBottomOnly(): Modifier {
    return drawBehind {
        val y = size.height - density / 2

        drawLine(
            Color.LightGray,
            Offset(0f, 0f),
            Offset(size.width, 0f),
            density
        )
        drawLine(
            Color.LightGray,
            Offset(0f, y),
            Offset(size.width, y),
            density
        )
    }
}
