package com.gabriel.formusic

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gabriel.formusic.service.MusicMediaPlayer
import com.gabriel.formusic.ui.components.CurrentPlayingSection
import com.gabriel.formusic.ui.components.MusicSearchBar
import com.gabriel.formusic.ui.musicList.MusicList
import com.gabriel.formusic.ui.musicList.MusicListItem
import com.gabriel.formusic.ui.musicList.MusicListViewModel
import com.gabriel.formusic.ui.theme.AzulClaro
import com.gabriel.formusic.ui.theme.AzulEscuro
import com.gabriel.formusic.ui.theme.JetpackComposeLearningTheme
import com.gabriel.formusic.ui.theme.Laranja
import com.gabriel.formusic.ui.theme.RosaEscuro
import com.gabriel.formusic.ui.theme.RoxoEscuro_3
import com.gabriel.formusic.ui.theme.TabSelectedBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {

    private var selectedFolderUri by mutableStateOf<Uri?>(null)
    private var show_btnSelectFolder by mutableStateOf(true)

    lateinit var currentPlayingSection:CurrentPlayingSection
    lateinit var saved_music_folder:SharedPreferences
    var savedFolderUriString: String? = null

    var carregouPasta:Boolean = false

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val musicMediaPlayerComponent: MusicMediaPlayer = MusicMediaPlayer(this, mediaPlayer)

    private val musicListViewModel:MusicListViewModel by viewModels()
    private var listComponent = MusicList(musicMediaPlayerComponent)
    private var searchBar = MusicSearchBar(listComponent)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentPlayingSection = CurrentPlayingSection(musicMediaPlayerComponent, musicListViewModel)
        saved_music_folder = getSharedPreferences("folderMusic", MODE_PRIVATE)
        // Tentando  obter pasta do SharedPreferences
        savedFolderUriString = saved_music_folder.getString("music_folder_uri", null)
        setContent {
            JetpackComposeLearningTheme {
                Surface {
                    linearGradient()
                    val navController = rememberNavController()
                    NavHost(navController = navController , startDestination = "musicListScreen"){
                        composable("musicListScreen"){ EnterAnimation{ MusicListScreen(navController)} }
                        composable("musicDetailScreen/{name}"){ backstackEntry -> EnterAnimation{ MusicDetailScreen(navController)} }
                    }
                }
            }
        }
    }

    @Composable
    fun MusicListScreen(navController: NavHostController) {
        var selectedMusic = musicListViewModel.selectedMusic

        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.size(15.dp))
            searchBar.render()
            headerMenu()

            val img_select_folder: Painter = painterResource(id = R.drawable.button_select_folder)
            if (savedFolderUriString == null && show_btnSelectFolder) { // Se usuário ainda não selecionou uma pasta.
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = img_select_folder,
                        contentDescription = "Imagem Clicável",
                        modifier = Modifier
                            .clickable(onClick = {
                                val musicsDirectory = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_MUSIC
                                )
                                openDirectory(musicsDirectory.toUri())
                                show_btnSelectFolder = false
                            })
                    )
                }
            }else if(carregouPasta == false){
                Log.i("loadFiles", "Carregando arquivos")
                var savedFolderUri:Uri? = savedFolderUriString?.let { Uri.parse(it) }
                if (savedFolderUri != null){
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    applicationContext.contentResolver.takePersistableUriPermission(savedFolderUri, takeFlags)
                    musicListViewModel.musicList = runBlocking { scanMusicFiles(savedFolderUri) }
                    carregouPasta = true
                }
            }

            Column( Modifier
                .weight(1f)) {
                listComponent.musicList = musicListViewModel.musicList
                listComponent.render(musicListViewModel)
            }

            Spacer(Modifier.size(10.dp))
            Log.i("Inform", "\n\n\nselectedMusic.value.t: ${selectedMusic.value.tituloMusica}\n\n\n")
            if(selectedMusic.value.tituloMusica != "") {
                currentPlayingSection.render( onClick = { navController.navigate("musicDetailScreen/${selectedMusic.value?.tituloMusica}") })
            }
        }
    }

    @Composable
    fun MusicDetailScreen(navController: NavHostController){

        var selectedMusic = musicListViewModel.selectedMusic.value
        var musicIsPlaying by remember { mutableStateOf(mediaPlayer.isPlaying) }

        Column {
            Column(modifier = Modifier.weight(3f).padding(horizontal = 20.dp)) {
                var musicImageCover = BitmapFactory.decodeByteArray(selectedMusic.photoCapa, 0, selectedMusic.photoCapa.size)

                Spacer(modifier = Modifier.padding(vertical = 40.dp))
                Image(bitmap = musicImageCover.asImageBitmap(), contentDescription = "",
                    Modifier
                        .fillMaxWidth()
                        .height((LocalConfiguration.current.screenHeightDp / 2.5).dp)
                        .clip(RoundedCornerShape(18))                       // clip to the circle shape
                )
            }


            // Column com: Titulo + Artista + Slider
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(2f)) {
                Text(text = "${selectedMusic.tituloMusica}", modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 10.dp), color = Color.White, textAlign = TextAlign.Center, fontSize = 19.sp, fontWeight = FontWeight.Bold
                )
                Text(text = "${selectedMusic.nomeArtista}", modifier = Modifier
                    .fillMaxWidth(), color = Color.White, textAlign = TextAlign.Center, fontSize = 14.sp
                )

                var progress by remember { mutableStateOf(0f) }

                LaunchedEffect(mediaPlayer) {
                    while (isActive) {
                        val currentPosition = mediaPlayer.currentPosition.toFloat()
                        val duration = mediaPlayer.duration.toFloat()

                        // Garanta que a duração não seja 0 para evitar divisão por zero
                        if (duration > 0f) {
                            progress = currentPosition / duration
                        } else {
                            progress = 0f
                        }

                        delay(200) // Ajuste isso para a taxa de atualização desejada
                    }
                }

                Slider(
                    value = progress,
                    onValueChange = { /* Você pode manipular o progresso do áudio aqui, se necessário */ },
                    colors = SliderDefaults.colors(
                        thumbColor = RosaEscuro,
                        activeTrackColor = Laranja,
                        inactiveTrackColor = AzulClaro,
                    ), modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            val playPauseIcon: Painter = if(musicIsPlaying) painterResource(id = R.drawable.pause_icon) else painterResource(id = R.drawable.play_icon)

            Row(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.previous_icon), contentDescription = "",
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .weight(.3f)
                )
                Image(painter = playPauseIcon, contentDescription = "",
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .weight(.3f)
                        .clickable {
                            if (mediaPlayer.isPlaying) {
                            musicIsPlaying = false
                            mediaPlayer.pause()
                        } else if (!selectedMusic.tituloMusica.isEmpty()) {
                            musicIsPlaying = true
                            mediaPlayer.start()
                        } }
                )
                Image(painter = painterResource(id = R.drawable.next_icon), contentDescription = "",
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .weight(.3f)
                )
            }
        }

    }



    @Composable
    fun headerMenu(){
        var tabIndex by remember {
            mutableStateOf(0)
        }

        val tabs = listOf("Músicas", "Álbuns")
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)){
            TabRow(selectedTabIndex = tabIndex, modifier = Modifier.weight(3f)) {
                tabs.forEachIndexed{index, title ->
                    var fontSize = if (tabIndex == index) 17.sp else 13.sp
                    Tab(selected = tabIndex == index,
                        selectedContentColor = TabSelectedBlue,
                        unselectedContentColor = RoxoEscuro_3,
                        onClick = { tabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp, 5.dp, 0.dp, 10.dp))
                        },
                        modifier = Modifier
                            .background(RoxoEscuro_3)
                    )
                }
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                val gear_icon:Painter = painterResource(id = R.drawable.gear_icon)
                Image(painter = gear_icon, contentDescription = "")
                Spacer(Modifier.size(25.dp))
            }

        }
    }


    fun openDirectory(pickerInitialUri: Uri){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val uri = result.data?.data
            Log.i("Shared", "ResultL URI: $uri")
            if (uri != null) {
                selectedFolderUri = uri
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
                Toast.makeText(this, "Selecionado: $uri", Toast.LENGTH_SHORT).show()
                with(saved_music_folder.edit()){
                    putString("music_folder_uri", uri?.toString())
                    apply()
                }
                musicListViewModel.musicList = runBlocking { scanMusicFiles(uri.normalizeScheme()) }
            } else {
                Toast.makeText(this, "Erro ao selecionar a pasta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun scanMusicFiles(folderUri: Uri): List<MusicListItem> {
        val musicFiles = mutableListOf<MusicListItem>()

        val folderDocumentFile = DocumentFile.fromTreeUri(this, folderUri)
        if (folderDocumentFile != null) {
            val dataRetriever:MediaMetadataRetriever = MediaMetadataRetriever()
            Log.i("Shared", "Começando leitura...")
            Log.i("Shared", "URI: ($folderUri)")
            for (file in folderDocumentFile.listFiles() ?: emptyArray()) {
                if (file.isFile && file.type == "audio/mpeg") {
                    dataRetriever.setDataSource(this, file.uri)
                    val musicTitle = dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown"
                    val artistName = dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown"
                    val musicPath = file.uri;
                    val musicDuration = Integer.parseInt(dataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
                    val coverPhoto = dataRetriever.embeddedPicture ?: ByteArray(0)

                    var durationMillis = musicDuration.toLong()
                    var minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
                    var song_seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(durationMillis))

                    val format = if(song_seconds.toString().length == 1) "%d:0%d" else "%d:%d"
                    var songDurationFormatedString = String.format(format,
                        TimeUnit.MILLISECONDS.toMinutes(durationMillis),
                        TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(durationMillis))
                    )

                    musicFiles.add(MusicListItem(R.drawable.disc_icon, musicTitle, artistName, coverPhoto, false, musicDuration, songDurationFormatedString, musicPath))
                }
            }
        }else{
            if(folderDocumentFile == null){
                Log.i("Shared", "folderDocumentFile é nulo")
            }else if(!folderDocumentFile.isDirectory){
                Log.i("Shared", "folderDocumentFile não é diretório")

            }
        }

        return musicFiles
    }


    @Preview(showBackground = true, name = "NEXUS_7", device = Devices.NEXUS_7)
    @Composable
    fun linearGradient() {
        val gradient = Brush.linearGradient(
            0.4f to RoxoEscuro_3,
            20.0f to AzulEscuro,
            start = Offset.Zero,
            end = Offset.Infinite
        )
        Box(modifier = Modifier
            .fillMaxSize()
            .background(gradient))
    }

    //@Preview
    @Composable
    fun primeiraTelaPreview(){
        JetpackComposeLearningTheme {
            Surface {
                //MusicListScreen()
            }
        }
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