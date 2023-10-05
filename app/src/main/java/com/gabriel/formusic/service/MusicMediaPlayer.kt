package com.gabriel.formusic.service

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.gabriel.formusic.ui.components.MusicListItem

class MusicMediaPlayer(context: Context, val mPlayer:MediaPlayer) {

    private val mediaPlayer:MediaPlayer = mPlayer
    private val context = context
    private var currentPlayingMusicDatasource:Uri = Uri.EMPTY

    fun reproduzirMusica(musica:MusicListItem){
        var musicaPath = musica.musicPath

        if(musicaPath != currentPlayingMusicDatasource){
            if (mediaPlayer.isPlaying){
                mediaPlayer.stop()
            }

            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, musicaPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }

        currentPlayingMusicDatasource = musicaPath
    }


}