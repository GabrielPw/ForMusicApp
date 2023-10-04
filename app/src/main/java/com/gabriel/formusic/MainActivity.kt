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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.gabriel.formusic.ui.components.CurrentPlayingSection
import com.gabriel.formusic.ui.components.MusicListItem
import com.gabriel.formusic.ui.components.TestComponents
import com.gabriel.formusic.ui.theme.AzulOpaco
import com.gabriel.formusic.ui.theme.JetpackComposeLearningTheme
import com.gabriel.formusic.ui.theme.LightTextColorPurple
import com.gabriel.formusic.ui.theme.LightTextColorPurple_2
import com.gabriel.formusic.ui.theme.RoxoClaroComTranparencia
import com.gabriel.formusic.ui.theme.RoxoEscuro_2


class MainActivity : ComponentActivity() {

    private var selectedFolderUri by mutableStateOf<Uri?>(null)
    private var musicList by mutableStateOf<List<MusicListItem>>(emptyList())
    private var show_btn_Image by mutableStateOf(true)
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    var currentPlayingMusicDatasource: Uri = Uri.EMPTY
    lateinit var currentPlayingSection:CurrentPlayingSection
    lateinit var saved_music_folder:SharedPreferences
    var savedFolderUriString: String? = null

    var carregouPasta:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentPlayingSection = CurrentPlayingSection(mediaPlayer)
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

            var components = TestComponents()
            Spacer(Modifier.size(15.dp))

            components.searchBar()
            components.headerMenu()

            // Botão para selectionar pasta
            val img_select_folder: Painter = painterResource(id = R.drawable.button_select_folder)

            if (savedFolderUriString == null && show_btn_Image) {
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
                                show_btn_Image = false
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
                    listaDeMusicas(it){ updatedSelectedMusic ->
                        selectedMusic = updatedSelectedMusic
                    }
                }
            }

            Spacer(Modifier.size(10.dp))
            selectedMusic?.let { if(selectedMusic!!.tituloMusica != "") currentPlayingSection.render(it) }

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

    @Preview
    @Composable
    fun primeiraTelaPreview(){
        JetpackComposeLearningTheme {
            Surface {
                primeiraTela()
            }
        }
    }

    @Composable
    fun linearGradient() {
        val gradient = Brush.linearGradient(
            0.0f to RoxoEscuro_2,
            500.0f to RoxoClaroComTranparencia,
            start = Offset.Zero,
            end = Offset.Infinite
        )
        Box(modifier = Modifier
            .fillMaxSize()
            .background(gradient))
    }

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
                        .width((38).dp)
                        .height((38).dp)
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
            Text(text = musicItem.musicDuration.toString(), color = Color.White, fontSize = 10.sp)
            Spacer(Modifier.size(10.dp))
        }
    }

    @Composable
    fun listaDeMusicas(selectedMusic:MusicListItem, onMusicSelected: (MusicListItem?) -> Unit){
        var localSelectedMusic: MusicListItem? by remember { mutableStateOf(selectedMusic) }

        Column {
            Row(Modifier.padding(horizontal = 20.dp)) {
                LazyColumn {
                    items(
                        count = musicList.size,
                        key = { it},
                        itemContent = {
                                index ->
                            var musicItem = musicList[index]
                            val isSelected = localSelectedMusic == musicItem
                            Column(Modifier.clip(shape = RoundedCornerShape(14.dp))){
                                musicBoxLayout(musicItem, isSelected, onClick = {
                                    localSelectedMusic = musicItem
                                    onMusicSelected(localSelectedMusic)
                                    if (localSelectedMusic == musicItem) {
                                        reproduzirMusica(musicItem)
                                    }
                                })
                            }
                            Spacer(modifier = Modifier.padding(10.dp))
                        }
                    )
                }
            }

        }
    }

    fun reproduzirMusica(musica:MusicListItem){

        var musicaPath = musica.musicPath

        if(musicaPath != currentPlayingMusicDatasource){
            if (mediaPlayer.isPlaying){
                mediaPlayer.stop()
            }

            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, musicaPath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }

        currentPlayingMusicDatasource = musicaPath
    }

}