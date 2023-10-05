package com.gabriel.formusic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.gabriel.formusic.ui.theme.LightTextColorPurple

class MusicSearchBar {

    @Composable
    fun render(){

        var text: String by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            ProvideTextStyle(TextStyle(color = LightTextColorPurple)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { newText: String -> text = newText },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}