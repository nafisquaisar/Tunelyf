package com.song.nafis.nf.TuneLyf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.song.nafis.nf.TuneLyf.Activity.BaseActivity
import com.song.nafis.nf.TuneLyf.Activity.PlayMusicStreamActivity
import com.song.nafis.nf.TuneLyf.Model.MusicDetail
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
import com.song.nafis.nf.TuneLyf.adapter.MusicAdapter
import com.song.nafis.nf.TuneLyf.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: MusicAdapter
    private val musicViewModel: MusicViewModel by viewModels()

    companion object {
        var musiclist: ArrayList<MusicDetail> = arrayListOf()
        lateinit var musiclistSerach: ArrayList<MusicDetail>
        var search: Boolean = false
        var sortOrder: Int = 0
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC",
            MediaStore.Audio.Media.DISPLAY_NAME
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        applyStatusBarScrim(binding.statusBarScrim)

        

        toolbar = binding.hometoolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title="My Music"
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.back_arrow)
        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.anticipate_interpolator, android.R.anim.anticipate_overshoot_interpolator)
        }

        clickbtn()

        if (requestRuntimePermission()) {
            adapterset()
        }
    }

    private fun fetchMusicData(): ArrayList<MusicDetail> {
        val templist = ArrayList<MusicDetail>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortingList[sortOrder],
            null
        )

        cursor?.use {
            val uri = Uri.parse("content://media/external/audio/albumart")
            while (cursor.moveToNext()) {
                val musicDetail = MusicDetail(
                    musicId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                    musicTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                    musicAlbum = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                    musicArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                    musicPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                    duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                    imgUri = Uri.withAppendedPath(uri, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)).toString()).toString(),
                    id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                )
                templist.add(musicDetail)
            }
        }

        return templist
    }

    private fun adapterset() {
        search = false
        val sortingEditor = getSharedPreferences("Sorting", MODE_PRIVATE)
        sortOrder = sortingEditor.getInt("sortOrder", 0)
        musiclist = fetchMusicData()
        val unifiedList = musiclist.map { it.toUnifiedMusic() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 123)
            }
        }


        adapter = MusicAdapter(this, musiclist) { clickedIndex ->
            val selectedSong = adapter.getMusicAt(clickedIndex)
            if (selectedSong == null) {
                Toast.makeText(this, "Song not found at that position", Toast.LENGTH_SHORT).show()
                return@MusicAdapter
            }

            val unifiedList = adapter.getCurrentUnifiedList()
            musicViewModel.setPlaylist(unifiedList)
            musicViewModel.setInitialIndex(clickedIndex,this)


            val intent = Intent(this, PlayMusicStreamActivity::class.java).apply {
                putParcelableArrayListExtra("SONG_LIST", ArrayList(unifiedList))
                putExtra("SONG_INDEX", clickedIndex)
                putExtra("SONG_TITLE", selectedSong.musicTitle)
                putExtra("SONG_TRACK", selectedSong.imgUri)
            }
            startActivity(intent)
        }
        adapter.updateMusicList(musiclist)


        binding.homeRecycler.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            adapter = this@MainActivity.adapter
        }

        binding.totalsong.text = "Total Songs : ${adapter.itemCount}"

        // 🟡 Empty state toggle
        val isEmpty = musiclist.isEmpty()
        binding.homeRecycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.totalsongLinearL.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.LocalShufflebtn.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyStateWrapper.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 101
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            adapterset()
        }
    }

    private fun clickbtn() {
        binding.apply {

            LocalShufflebtn.setOnClickListener {
                if (musiclist.isNotEmpty()) {
                    val unifiedList = musiclist.map { it.toUnifiedMusic() }.shuffled()
                    musicViewModel.setPlaylist(unifiedList)
                    musicViewModel.setInitialIndex(0,this@MainActivity)

                    val intent = Intent(this@MainActivity, PlayMusicStreamActivity::class.java).apply {
                        putParcelableArrayListExtra("SONG_LIST", ArrayList(unifiedList))
                        putExtra("SONG_INDEX", 0)
                        putExtra("SONG_TITLE", unifiedList[0].musicTitle)
                        putExtra("SONG_TRACK", unifiedList[0].imgUri)
                    }

                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "No songs available to shuffle", Toast.LENGTH_SHORT).show()
                }
            }
            binding.swipeRefresh.setOnRefreshListener {
                adapterset()
                binding.swipeRefresh.isRefreshing = false
            }



//            NowPlaying.setOnClickListener {
//                startActivity(Intent(this@MainActivity, PlayMusicStreamActivity::class.java))
//            }
            sortbutton.setOnClickListener {
                sortingFun()
            }
        }
    }

    private fun sortingFun() {
        val menuList = arrayOf("Recently Added", "Song Title", "File Size", "File Name")
        AlertDialog.Builder(this)
            .setTitle("Sort By")
            .setSingleChoiceItems(menuList, sortOrder) { _, which ->
                sortOrder = which
            }
            .setPositiveButton("OK") { _, _ ->
                getSharedPreferences("Sorting", MODE_PRIVATE).edit().apply {
                    putInt("sortOrder", sortOrder)
                    apply()
                }
                musiclist = fetchMusicData()
                adapter.updateMusicList(musiclist)
                binding.totalsong.text = "Total Songs: ${adapter.itemCount}"
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create().apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.icon_color))
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.icon_color))
            }
    }

    override fun onResume() {
        super.onResume()
        val sortValue = getSharedPreferences("Sorting", MODE_PRIVATE).getInt("sortOrder", 0)
        if (sortOrder != sortValue) {
            sortOrder = sortValue
            musiclist = fetchMusicData()
            adapter.updateMusicList(musiclist)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.playlist_menu, menu)
        val searchView = menu?.findItem(R.id.search_playlist)?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musiclistSerach = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    musiclistSerach.addAll(musiclist.filter {
                        it.musicTitle.lowercase().contains(userInput)
                    })
                    search = true
                    adapter.updateMusicList(searchList = musiclistSerach)
                }
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }


}

