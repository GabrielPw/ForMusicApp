package com.gabriel.formusic.ui.components

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gabriel.formusic.R
import com.gabriel.formusic.service.MusicMediaPlayer
import com.gabriel.formusic.ui.musicList.MusicListViewModel
import com.gabriel.formusic.ui.theme.Laranja
import com.gabriel.formusic.ui.theme.RoxoEscuro_2

class CurrentPlayingSection(musicMediaPlayer:MusicMediaPlayer, musicListViewModel: MusicListViewModel) {

    val musicListViewModel:MusicListViewModel = musicListViewModel
    var primeroUpdate = false
    val musicMediaPlayer = musicMediaPlayer
    var updateUI = mutableStateOf(true)

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun render(onClick: () -> Unit){
        var selectedMusic = musicListViewModel.selectedMusic.value
        var musicIsPlaying by remember { mutableStateOf(true) }
        val context = LocalContext.current

        Row(
            Modifier
                .background(RoxoEscuro_2)
                .height((65).dp)
                .clickable { onClick() }
            , verticalAlignment = Alignment.CenterVertically
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
                Text(text = "${selectedMusic.tituloMusica}", color = Color.White, fontSize = 15.sp, modifier = Modifier.basicMarquee())
                Text(text = "${selectedMusic.nomeArtista}", color = Color.White, fontSize = 12.sp)

                Spacer(modifier = Modifier.padding((15).dp))
                musicPlayerProgressBar(mediaPlayer = musicMediaPlayer.mPlayer)
            }

            musicIsPlaying = musicMediaPlayer.mPlayer.isPlaying

            val playPauseIcon: Painter = if(musicIsPlaying) painterResource(id = R.drawable.pause_icon) else painterResource(id = R.drawable.play_icon)
            Spacer(Modifier.size(15.dp))
            Image(painter = playPauseIcon, contentDescription = "",
                Modifier
                    .clickable {
                        if (musicMediaPlayer.mPlayer.isPlaying) {
                            musicIsPlaying = false
                            musicMediaPlayer.mPlayer.pause()
                            playPauseIcon
                        } else if (!selectedMusic.tituloMusica.isEmpty()) {
                            musicIsPlaying = true
                            musicMediaPlayer.mPlayer.start()
                        }
                    }
                    .size(34.dp))
            Spacer(Modifier.size(15.dp))

            if (primeroUpdate == false){
                musicIsPlaying = !musicIsPlaying
                musicIsPlaying = !musicIsPlaying

                primeroUpdate = true
            }
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

    @Composable
    fun EnterAnimation(content: @Composable () -> Unit) {
        AnimatedVisibility(
            visibleState = MutableTransitionState(
                initialState = false
            ).apply { targetState = true },
            modifier = Modifier,
            enter = slideInVertically(
                initialOffsetY = { -40 }
            ) + expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically() + shrinkVertically() + fadeOut(),
        ) {
            content()
        }
    }

}
