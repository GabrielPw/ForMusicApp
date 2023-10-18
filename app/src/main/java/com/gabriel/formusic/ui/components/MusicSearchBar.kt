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
import com.gabriel.formusic.ui.musicList.MusicList
import com.gabriel.formusic.ui.theme.LightTextColorPurple

class MusicSearchBar(musicList: MusicList) {

    var musicList = musicList
    @Composable
    fun render(){

        var query: String by remember { mutableStateOf("") }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            ProvideTextStyle(TextStyle(color = LightTextColorPurple)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { onQueryChanged ->
                        query = onQueryChanged
                        if(onQueryChanged.isNotEmpty()){
                            musicList.performQuery(query)
                        }else{
                            musicList.filteredList = emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}