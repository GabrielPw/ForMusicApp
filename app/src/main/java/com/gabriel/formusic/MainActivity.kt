package com.gabriel.formusic

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.gabriel.formusic.service.MusicMediaPlayer
import com.gabriel.formusic.ui.components.CurrentPlayingSection
import com.gabriel.formusic.ui.components.MusicList
import com.gabriel.formusic.ui.components.MusicListItem
import com.gabriel.formusic.ui.components.MusicSearchBar
import com.gabriel.formusic.ui.theme.AzulEscuro
import com.gabriel.formusic.ui.theme.JetpackComposeLearningTheme
import com.gabriel.formusic.ui.theme.RoxoEscuro_3


class MainActivity : ComponentActivity() {

    private var selectedFolderUri by mutableStateOf<Uri?>(null)
    private var musicList by mutableStateOf<List<MusicListItem>>(emptyList())
    private var show_btnSelectFolder by mutableStateOf(true)

    lateinit var currentPlayingSection:CurrentPlayingSection
    lateinit var saved_music_folder:SharedPreferences
    var savedFolderUriString: String? = null

    var carregouPasta:Boolean = false

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val musicMediaPlayerComponent: MusicMediaPlayer = MusicMediaPlayer(this, mediaPlayer)
    private var listComponent = MusicList(musicMediaPlayerComponent)
    private var searchBar = MusicSearchBar()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentPlayingSection = CurrentPlayingSection(musicMediaPlayerComponent)
        saved_music_folder = getSharedPreferences("folderMusic", MODE_PRIVATE)
        // Tentando  obter pasta do SharedPreferences
        savedFolderUriString = saved_music_folder.getString("music_folder_uri", null)
        setContent {
            JetpackComposeLearningTheme {
                Surface {
                    linearGradient()
                    primeiraTela()
                }
            }
        }
    }

    @Composable
    fun primeiraTela(){
        var selectedMusic by remember{ mutableStateOf<MusicListItem?>(MusicListItem(0, "","", ByteArray(0), false, 0, Uri.EMPTY)) }

        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.size(15.dp))
            searchBar.render()
            headerMenu()

            // Botão para selectionar pasta
            val img_select_folder: Painter = painterResource(id = R.drawable.button_select_folder)
            // Se usuário ainda não selecionou uma pasta.
            if (savedFolderUriString == null && show_btnSelectFolder) {
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
                var savedFolderUri:Uri? = savedFolderUriString?.let { Uri.parse(it) }
                if (savedFolderUri != null){
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    applicationContext.contentResolver.takePersistableUriPermission(savedFolderUri, takeFlags)
                    musicList = scanMusicFiles(savedFolderUri)
                    carregouPasta = true
                }
            }

            Column( Modifier
                .weight(1f)) {
                selectedMusic?.let {
                    listComponent.musicList = musicList
                    listComponent.render(selectedMusic = it, onMusicSelected = { updatedSelectedMusic -> selectedMusic = updatedSelectedMusic})
                }
            }

            Spacer(Modifier.size(10.dp))
            selectedMusic?.let { if(selectedMusic!!.tituloMusica != "") currentPlayingSection.render(it) }
        }
    }

    @Composable
    fun headerMenu(){
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
                musicList = scanMusicFiles(uri.normalizeScheme())
            } else {
                Toast.makeText(this, "Erro ao selecionar a pasta", Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun scanMusicFiles(folderUri: Uri): List<MusicListItem> {
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

                    musicFiles.add(MusicListItem(R.drawable.disc_icon, musicTitle, artistName, coverPhoto, false, musicDuration, musicPath))
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
            0.2f to RoxoEscuro_3,
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
                primeiraTela()
            }
        }
    }
}