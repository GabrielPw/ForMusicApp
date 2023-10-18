package com.gabriel.formusic.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gabriel.formusic.R
import com.gabriel.formusic.service.MusicMediaPlayer
import com.gabriel.formusic.ui.theme.AzulOpaco
import com.gabriel.formusic.ui.theme.LightTextColorPurple
import com.gabriel.formusic.ui.theme.LightTextColorPurple_2
import com.gabriel.formusic.ui.theme.RoxoEscuro_2

class MusicList(musicMediaPlayer: MusicMediaPlayer) {

    val musicMediaPlayer = musicMediaPlayer
    var musicList by mutableStateOf<List<MusicListItem>>(emptyList())
    var filteredList by mutableStateOf<List<MusicListItem>>(emptyList())

    @Composable
    fun musicBoxLayout(musicItem: MusicListItem,
                       isSelected: Boolean,
                       onClick: () -> Unit){

        var backgroundColor = RoxoEscuro_2

        if (isSelected) {
            backgroundColor = AzulOpaco
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .clickable { onClick() }
            .border(BorderStroke(1.dp, AzulOpaco), shape = RoundedCornerShape(14.dp))
            .clip(shape = RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(5.dp)) {

            val discIcon: Painter = painterResource(id = R.drawable.disc_icon)
            val imageCapa = BitmapFactory.decodeByteArray(musicItem.photoCapa, 0, musicItem.photoCapa.size);
            Spacer(Modifier.size(15.dp))

            if (imageCapa == null){
                Image(painter = discIcon, contentDescription = "")
            }else {
                Image(bitmap = imageCapa.asImageBitmap(), contentDescription = "",
                    Modifier
                        .width((48).dp)
                        .height((48).dp)
                        .clip(RoundedCornerShape(8.dp)))
            }
            Spacer(Modifier.size(15.dp))
            Column(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .weight(1f)) {
                Text(text = musicItem.tituloMusica, color = LightTextColorPurple_2, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = musicItem.nomeArtista, color = LightTextColorPurple, fontSize = 12.sp)
            }
            if(isSelected){
                val dots_icon: Painter = painterResource(id = R.drawable.dots_icon)
                Image(painter = dots_icon, contentDescription = "")
            }else{
                Text(text = musicItem.musicDurationFormatedString, color = Color.White, fontSize = 10.sp)
            }
            Spacer(Modifier.size(10.dp))
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun render(selectedMusic:MusicListItem, onMusicSelected: (MusicListItem?) -> Unit){
        var localSelectedMusic: MusicListItem? by remember { mutableStateOf(selectedMusic) }

        Row(Modifier.padding(horizontal = 20.dp)) {
            LazyColumn {
                items(
                    count = if (filteredList.isEmpty()) musicList.size else filteredList.size,
                    key = {it},
                    itemContent = {
                            index ->
                        var musicItem = if (filteredList.isEmpty()) musicList[index] else filteredList[index]
                        val isSelected = localSelectedMusic == musicItem
                        musicBoxLayout(musicItem, isSelected, onClick = {
                            localSelectedMusic = musicItem
                            onMusicSelected(localSelectedMusic)
                            if (localSelectedMusic == musicItem) {
                                musicMediaPlayer.reproduzirMusica(musicItem)
                            }
                        })
                        Modifier.animateItemPlacement()
                        Spacer(modifier = Modifier.padding(10.dp))
                    }
                )
            }
        }

        musicMediaPlayer.mPlayer.setOnCompletionListener {
            var finishedSongIndex = musicList.indexOf(selectedMusic)
            var nextSongToBePlayed = if(finishedSongIndex + 1 >= musicList.size) 0 else finishedSongIndex + 1
            musicMediaPlayer.reproduzirMusica(musicList[nextSongToBePlayed])
            localSelectedMusic = musicList[nextSongToBePlayed]
            onMusicSelected(localSelectedMusic)
        }
    }

    fun performQuery(query:String){

        var newFilteredList = musicList.filter { music -> music.tituloMusica.contains(query, ignoreCase = true) }
        this.filteredList = newFilteredList
    }
}