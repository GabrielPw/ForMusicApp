package com.gabriel.formusic.ui.components

import android.net.Uri

data class MusicListItem(val photoCapaDefault: Int, val tituloMusica:String, val nomeArtista:String, val photoCapa: ByteArray, var isSelected: Boolean = false, val musicDuration:Int,val musicDurationFormatedString:String , val musicPath: Uri)