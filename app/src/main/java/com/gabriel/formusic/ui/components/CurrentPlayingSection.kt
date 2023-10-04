package com.gabriel.formusic.ui.components

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gabriel.formusic.R
import com.gabriel.formusic.ui.theme.Laranja
import com.gabriel.formusic.ui.theme.RoxoEscuro_2

class CurrentPlayingSection(mediaPlayer:MediaPlayer) {

    val mediaPlayer = mediaPlayer
    @Composable
    fun render(selectedMusic: MusicListItem){
        var musicIsPlaying by remember { mutableStateOf(true) }

        Row(
            Modifier
                .background(RoxoEscuro_2)
//                .border(
//                    width = 1.dp,
//                    color = AzulClaro,
//                    shape = RoundedCornerShape(5.dp)
//                )
                .height((65).dp), verticalAlignment = Alignment.CenterVertically
        ) {
            val discIcon: Painter = painterResource(id = R.drawable.disc_icon)
            val imageCapa = BitmapFactory.decodeByteArray(selectedMusic.photoCapa, 0, selectedMusic.photoCapa.size);
            if (imageCapa == null){
                Image(painter = discIcon, contentDescription = "", Modifier.size(34.dp))
            }else {
                Image(bitmap = imageCapa.asImageBitmap(), contentDescription = "", Modifier.fillMaxHeight())
            }
            Column(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Log.i("UPDATE_", "render: ${selectedMusic.tituloMusica}")
                Text(text = "${selectedMusic.tituloMusica}", color = Color.White, fontSize = 14.sp)
                Text(text = "${selectedMusic.nomeArtista}", color = Color.White, fontSize = 12.sp)

                Spacer(modifier = Modifier.padding((15).dp))
                musicPlayerProgressBar(mediaPlayer = mediaPlayer)
            }
            val playPauseIcon: Painter = if(musicIsPlaying) painterResource(id = R.drawable.pause_icon) else painterResource(id = R.drawable.play_icon)
            Spacer(Modifier.size(15.dp))
            Image(painter = playPauseIcon, contentDescription = "",
                Modifier
                    .clickable {

                        if (mediaPlayer.isPlaying) {
                            musicIsPlaying = false
                            mediaPlayer.pause()
                            playPauseIcon
                        } else if (!selectedMusic.tituloMusica.isEmpty()) {
                            musicIsPlaying = true
                            mediaPlayer.start()
                        }
                    }
                    .size(34.dp))
            Spacer(Modifier.size(15.dp))
        }
    }

    @Composable
    fun musicPlayerProgressBar(
        mediaPlayer: MediaPlayer
    ) {
        val currentPosition = rememberUpdatedState(mediaPlayer.currentPosition)
        val totalDuration = rememberUpdatedState(mediaPlayer.duration)

        val progress = if (totalDuration.value > 0) {
            (currentPosition.value.toFloat() / totalDuration.value.toFloat())
        } else {
            0f
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Laranja, // Cor da barra de progresso
        )
    }
}
