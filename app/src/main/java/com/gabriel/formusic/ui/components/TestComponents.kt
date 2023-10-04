package com.gabriel.formusic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gabriel.formusic.R
import com.gabriel.formusic.ui.theme.AzulClaro
import com.gabriel.formusic.ui.theme.LightTextColorPurple
import com.gabriel.formusic.ui.theme.RoxoClaro

class TestComponents {

    //Preview(showBackground = true)
    @Composable
    fun headerMenu(){
        Column() {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically){

                Row(
                    Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .weight(1f), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(text = "Musicas", Modifier.padding(10.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = "Albuns", color = Color.White)
                }

                val gear_icon:Painter = painterResource(id = R.drawable.gear_icon)
                Image(painter = gear_icon, contentDescription = "")
                Spacer(Modifier.size(25.dp))
            }


        }
    }

    @Preview(showBackground = true)
    @Composable
    fun searchBar(){

        var text: String by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            ProvideTextStyle(androidx.compose.ui.text.TextStyle(color = LightTextColorPurple)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { newText: String -> text = newText },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun musicBoxLayout(musicName:String = "One", artistName:String = "Metallica", musicDuration:String = "2:30"){

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {

            val discIcon:Painter = painterResource(id = R.drawable.disc_icon)
            Spacer(Modifier.size(15.dp))
            Image(painter = discIcon, contentDescription = "")
            Spacer(Modifier.size(15.dp))
            Column(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .weight(1f)) {
                Text(text = musicName, color = LightTextColorPurple, fontSize = 14.sp)
                Text(text = artistName, color = LightTextColorPurple, fontSize = 12.sp)

            }
            Text(text = musicDuration, color = Color.White, fontSize = 10.sp)
            Spacer(Modifier.size(15.dp))
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun currentPlayingSection(musicName:String = "", artistName:String = "") {
        Row(
            Modifier
                .background(RoxoClaro)
                .border(
                    width = 1.dp,
                    color = AzulClaro,
                    shape = RoundedCornerShape(5.dp)
                )
                .height((65).dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {

            val discIcon: Painter = painterResource(id = R.drawable.disc_icon)
            Spacer(Modifier.size(15.dp))
            Image(painter = discIcon, contentDescription = "", Modifier.size(34.dp))
            Column(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(text = musicName, color = Color.White, fontSize = 14.sp)
                Text(text = artistName, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.padding((15).dp))
            }
            val playIcon: Painter = painterResource(id = R.drawable.play_icon)
            Spacer(Modifier.size(15.dp))
            Image(painter = playIcon, contentDescription = "")
            Spacer(Modifier.size(38.dp))
        }
    }


    //@Preview(showBackground = true)
    @Composable
    fun listaDeMusicas(listaMusicas:List<MusicListItem>){

        LazyColumn {
            items(listaMusicas){
                musica ->
                musicBoxLayout(musica.tituloMusica, musica.nomeArtista)
                Spacer(modifier = Modifier.size((16).dp))
            }
        }
    }
}