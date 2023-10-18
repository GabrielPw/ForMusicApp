package com.gabriel.formusic.ui.musicList

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MusicListViewModel : ViewModel() {

    var selectedMusic = mutableStateOf(MusicListItem(0, "","", ByteArray(0), false, 0, "", Uri.EMPTY))
    var musicList by mutableStateOf<List<MusicListItem>>(emptyList())

}