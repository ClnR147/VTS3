package com.example.vtsdaily3.ui.preview

import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview(showBackground = true)
@Composable
fun ShapePreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShapeSample("Rectangle", RectangleShape)
        ShapeSample("Circle", CircleShape)
        ShapeSample("Rounded 8dp", RoundedCornerShape(8.dp))
        ShapeSample("Rounded 16dp", RoundedCornerShape(16.dp))
        ShapeSample("Cut 8dp", CutCornerShape(8.dp))
    }
}

@Composable
private fun ShapeSample(label: String, shape: Shape) { // 👈 THIS LINE FIXES EVERYTHING
    Column {
        Text(label)

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}