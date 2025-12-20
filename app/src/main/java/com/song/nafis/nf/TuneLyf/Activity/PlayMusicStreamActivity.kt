    package com.song.nafis.nf.TuneLyf.Activity

    import android.Manifest
    import android.content.Context
    import android.content.Intent
    import android.media.AudioManager
    import android.media.audiofx.AudioEffect
    import android.os.Build
    import android.os.Bundle
    import android.view.View
    import android.widget.SeekBar
    import android.widget.Toast
    import androidx.activity.viewModels
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import com.bumptech.glide.Glide
    import com.google.android.material.bottomsheet.BottomSheetDialog
    import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
    import com.song.nafis.nf.TuneLyf.R
    import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
    import com.song.nafis.nf.TuneLyf.databinding.ActivityPlayMusicStreamBinding
    import com.song.nafis.nf.TuneLyf.resource.Resource
    import dagger.hilt.android.AndroidEntryPoint
    import timber.log.Timber
    import android.os.Handler
    import android.os.VibrationEffect
    import android.os.Vibrator
    import android.widget.ImageView
    import android.widget.LinearLayout
    import androidx.annotation.RequiresPermission
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.widget.AppCompatButton
    import androidx.core.content.FileProvider
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.material.card.MaterialCardView
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import com.google.android.material.textfield.TextInputLayout
    import com.google.firebase.auth.FirebaseAuth
    import com.song.nafis.nf.TuneLyf.BroadReciver.PlaybackControlHolder
    import com.song.nafis.nf.TuneLyf.BroadReciver.PlaybackControlHolder.PlayerControlListener
    import com.song.nafis.nf.TuneLyf.Entity.FavoriteEntity
    import com.song.nafis.nf.TuneLyf.Entity.PlaylistEntity
    import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity
    import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline
    import com.song.nafis.nf.TuneLyf.UI.FavoriteViewModel
    import com.song.nafis.nf.TuneLyf.UI.PlaylistViewModel
    import com.song.nafis.nf.TuneLyf.UI.RecentlyPlayedViewModel
    import com.song.nafis.nf.TuneLyf.adapter.PlaylistSelectAdapter
    import java.io.File

    @AndroidEntryPoint
    class PlayMusicStreamActivity : BaseActivity() {

        private lateinit var binding: ActivityPlayMusicStreamBinding
        // With this:
        private val viewModel: MusicViewModel by viewModels()
        private val playlistViewModel: PlaylistViewModel by viewModels()
        private val favoriteViewModel: FavoriteViewModel by viewModels()
        private val recentlyPlayedViewModel: RecentlyPlayedViewModel by viewModels()
        private var serviceStarted = false




        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityPlayMusicStreamBinding.inflate(layoutInflater)
            setContentView(binding.root)

            applyStatusBarScrim(binding.statusBarScrim)


            val songTitle = intent.getStringExtra("SONG_TITLE") ?: "Unknown"
            val songArtwork = intent.getStringExtra("SONG_TRACK") ?: "Unknown"

            val songList = intent.getParcelableArrayListExtra<UnifiedMusic>("SONG_LIST") ?: arrayListOf()
            val currentIndex = intent.getIntExtra("SONG_INDEX", 0)

            val incomingSong = if (songList.isNotEmpty() && currentIndex in songList.indices) {
                songList[currentIndex]
            } else {
                null
            }

            val currentSong = viewModel.currentUnifiedSong.value
            val currentPlaylist = viewModel.playlistLiveData.value

            observeData()

            val isSameSong = currentSong?.musicId == incomingSong?.musicId
            val isSamePlaylist = currentPlaylist?.map { it.musicId } == songList.map { it.musicId }

            val shouldRestart = !(isSameSong && isSamePlaylist)

            if (shouldRestart) {
                Timber.d("🚀 Updating playlist and index...")
                viewModel.setPlaylist(songList)
                viewModel.setInitialIndex(currentIndex, this)
            } else {
                Timber.d("✅ Already playing — no need to reinitialize.")
            }




            setupUI(songArtwork,songTitle)

            // In PlayMusicStreamActivity's onCreate()
            PlaybackControlHolder.listener = object : PlayerControlListener {


                override fun onPlayPause() {
                    runOnUiThread {
                        viewModel.playPauseToggle(this@PlayMusicStreamActivity)
                    }            }

                override fun onNext() {
                    runOnUiThread {
                        viewModel.nextSong()
                    }
                }

                override fun onPrev() {
                    runOnUiThread {
                        viewModel.previousSong()
                    }
                }

                override fun onExit() {
                    runOnUiThread {
                        finish()
                    }
                }

                override fun isPlaying(): Boolean {
                    return viewModel.isPlaying.value ?: false
                }
            }


            ensureService()   // ✅ yahin call

        }



        private fun ensureService() {
            if (serviceStarted) return

            val intent = Intent(this, MusicServiceOnline::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }

            serviceStarted = true
        }



        private fun setupUI(songArtwork: String, songTitle: String) {
            binding.playMusicTitle.isSelected = true

            binding.playMusicplaypausebtn.setOnClickListener {
                vibrate(this)
                viewModel.playPauseToggle(this)
            }
            binding.playMusicNextbtn.setOnClickListener {
                Timber.tag("test").d("Next button clicked")
                vibrate(this)
                viewModel.nextSong()
            }
            binding.playMusicPreviousbtn.setOnClickListener {
                vibrate(this)
                Timber.tag("test").d("Previous button clicked")
                viewModel.previousSong()
            }
            binding.playMusicequiliserbtn.setOnClickListener {
                val sessionId = viewModel.getAudioSessionId()
                try {
                    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }
                    startActivityForResult(intent, 0)
                } catch (e: Exception) {
                    Toast.makeText(this, "No equalizer found on this device", Toast.LENGTH_SHORT).show()
                }
            }

            binding.playMuiscBackbtn.setOnClickListener {
                finish()
            }
            binding.repeat.setOnClickListener {
                viewModel.toggleRepeatMode()

                // Optionally update UI
                val isRepeat = viewModel.isRepeatMode.value ?: false
                val icon = if (isRepeat) R.drawable.repeat_one_24px else R.drawable.baseline_repeat_24
                binding.repeat.setImageResource(icon)
            }

            binding.sharebtn.setOnClickListener {

                val song = viewModel.currentUnifiedSong.value

                if (song == null) {
                    Toast.makeText(this, "No song is playing", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val songTitle = song.musicTitle.ifBlank { "My Favorite Song" }

                // ✅ LOCAL SONG → share audio file
                if (song.isLocal) {

                    val audioFile = File(song.musicPath)
                    if (!audioFile.exists()) {
                        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val audioUri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.provider",
                        audioFile
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, audioUri)
                        putExtra(Intent.EXTRA_TEXT, songTitle)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(shareIntent, "Share song via"))

                }
                // ✅ ONLINE SONG → share app link
                else {

                    val playStoreLink =
                        "https://play.google.com/store/apps/details?id=com.song.nafis.nf.TuneLyf"

                    val shareText =
                        "🎵 $songTitle\n\n" +
                                "Is song ko sunne ke liye TuneLyf app download kijiye 👇\n" +
                                playStoreLink

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }

                    startActivity(Intent.createChooser(shareIntent, "Share song via"))
                }
            }


            binding.stopTimer.setOnClickListener {
                val isTimerRunning = viewModel.timerRunning.value == true

                if (!isTimerRunning) {
                    showBottomSheetDialog() // show 15, 30, 60 options
                } else {
                    // Show confirmation to cancel timer
                    val builder = MaterialAlertDialogBuilder(this)
                        .setTitle("Stop Timer")
                        .setMessage("Do you want to cancel the active timer?")
                        .setPositiveButton("Yes") { _, _ ->
                            viewModel.cancelStopTimer()
                            Toast.makeText(this, "Timer cancelled", Toast.LENGTH_SHORT).show()
                            binding.stopTimer.setColorFilter(ContextCompat.getColor(this, R.color.icon_color))
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }

                    val dialog = builder.create()
                    dialog.show()

                    // Set button text color
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        ?.setTextColor(ContextCompat.getColor(this, R.color.icon_color))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        ?.setTextColor(ContextCompat.getColor(this, R.color.icon_color))
                }
            }

            binding.volumeBtn.setOnClickListener {
                showVerticalVolumeDialog()
            }

            binding.addToPlaylistBtn.setOnClickListener {
                viewModel.currentUnifiedSong.value?.let { current ->
                    showAddToPlaylistDialog(current)
                }
            }

            binding.seekBackBtn.setOnClickListener {
                vibrate(this)
                viewModel.seekBy(-5000) // ⏪ 5 sec
            }

            binding.seekForwardBtn.setOnClickListener {
                vibrate(this)
                viewModel.seekBy(5000) // ⏩ 5 sec
            }



            // 🔥 Set the listener after ViewModel is ready

            Glide.with(this)
                .load(songArtwork)
                .placeholder(R.mipmap.logo)   // loading ke time
                .error(R.mipmap.logo)         // ❗ 404 / broken URL ke liye
                .into(binding.playmusicImg)


            binding.playMusicSeek.max = 0
            binding.musicEndTime.text = "00:00"
            binding.musicTimeStart.text = "00:00"

            binding.playMusicTitle.isSelected = true


            binding.playMusicSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) viewModel.seekTo(progress.toLong())
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        private fun observeData() {
            viewModel.currentSongTitle.observe(this) {
                binding.playMusicTitle.text = it
            }
            viewModel.currentSongArtwork.observe(this) {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.mipmap.music_icon)
                    .into(binding.playmusicImg)
            }
            viewModel.currentDuration.observe(this) { durationStr ->
                if (durationStr.matches(Regex("\\d{2}:\\d{2}"))) {
                    binding.musicEndTime.text = durationStr

                    val (min, sec) = durationStr.split(":").map { it.toIntOrNull() ?: 0 }
                    val durationMs = (min * 60 + sec) * 1000
                    binding.playMusicSeek.max = durationMs
                } else {
                    // Invalid format, hide or set default
                    binding.musicEndTime.text = "00:00"
                    binding.playMusicSeek.max = 0
                }
            }

            viewModel.currentPosition.observe(this) {
                binding.musicTimeStart.text = it
                val duration = binding.playMusicSeek.max
                val pos = try {
                    viewModel.currentPosition.value?.let { time ->
                        val (min, sec) = time.split(":").map { it.toLong() }
                        ((min * 60 + sec) * 1000).toInt()
                    } ?: 0
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }

                binding.playMusicSeek.progress = pos
            }

            viewModel.isRepeatMode.observe(this) { isRepeat ->
                val icon = if (isRepeat) R.drawable.repeat_one_24px else R.drawable.baseline_repeat_24
                binding.repeat.setImageResource(icon)
            }

            viewModel.timerRunning.observe(this) { isRunning ->
                if (isRunning) {
                    binding.stopTimer.setColorFilter(ContextCompat.getColor(this, R.color.secondary_dark_blue))
                } else {
                    binding.stopTimer.setColorFilter(ContextCompat.getColor(this, R.color.secondary_icon_color))
                }
            }


            viewModel.isBuffering.observe(this) { isLoading ->
                if (isLoading) {
                    binding.playMusicplaypausebtn.visibility = View.GONE
                    binding.loadingContainer.visibility=View.VISIBLE
                    binding.loadingSpinner.visibility = View.VISIBLE
                } else {
                    binding.loadingContainer.visibility=View.GONE
                    binding.playMusicplaypausebtn.visibility = View.VISIBLE
                    binding.loadingSpinner.visibility = View.GONE
                }
            }

            viewModel.isPlaying.observe(this) { isPlaying ->
                val buffering = viewModel.isBuffering.value ?: false
                if (!buffering) {
                    binding.playMusicplaypausebtn.setImageResource(
                        if (isPlaying) R.drawable.pause_button else R.drawable.playbtn
                    )
                }
            }


            viewModel.currentUnifiedSong.observe(this) { song ->
                    if (song != null) {
                        recentlyPlayedViewModel.addToRecentlyPlayed(song)

                        favoriteViewModel.isFavorite(song.musicId).observe(this) { isFav ->
                            val icon = if (isFav) R.drawable.favoritefull else R.drawable.empty_heart
                            binding.playMusicfavoritebtn.setImageResource(icon)

                            binding.playMusicfavoritebtn.setOnClickListener {
                                val favEntity = FavoriteEntity(
                                    id = song.musicId,
                                    title = song.musicTitle,
                                    artist = song.musicArtist,
                                    artworkUrl = song.imgUri,
                                    durationMs = song.duration,
                                    audioUrl = song.musicPath,
                                    isLocal = song.isLocal,
                                    album = song.musicAlbum
                                )

                                if (isFav) {
                                    favoriteViewModel.removeFromFavorite(favEntity)
                                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                } else {
                                    favoriteViewModel.addToFavorite(favEntity)
                                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        // ✅ Compare with current playing song

                    }
                }


        }

        private fun showBottomSheetDialog() {
            // Cancel any existing timer first
            viewModel.cancelStopTimer()

            val dialog = BottomSheetDialog(this)
            dialog.setContentView(R.layout.timer_dilaog)
            dialog.show()

            fun setTimer(minutes: Int) {
                Toast.makeText(this, "Music will stop after $minutes minutes", Toast.LENGTH_SHORT).show()
                viewModel.startStopTimer(minutes)
                binding.stopTimer.setColorFilter(ContextCompat.getColor(this, R.color.secondary_dark_blue))
                dialog.dismiss()
            }

            dialog.findViewById<LinearLayout>(R.id.time15min)?.setOnClickListener {
                setTimer(15)
            }

            dialog.findViewById<LinearLayout>(R.id.time30min)?.setOnClickListener {
                setTimer(30)
            }

            dialog.findViewById<LinearLayout>(R.id.time60min)?.setOnClickListener {
                setTimer(60)
            }
        }

        private fun showVerticalVolumeDialog() {
            val dialogView = layoutInflater.inflate(R.layout.volume_dialog, null)
            val seekBar = dialogView.findViewById<SeekBar>(R.id.volumeSeekBar)

            val dialog = BottomSheetDialog(this)
            dialog.setContentView(dialogView)
            dialog.show()

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            // Set initial progress
            seekBar.progress = (currentVolume * 100) / maxVolume

            // Seekbar listener
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    val newVolume = (progress * maxVolume) / 100
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        private fun showAddToPlaylistDialog(currentSong: UnifiedMusic?) {
            val bottomSheetDialog = BottomSheetDialog(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_to_playlist, null)

            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvPlaylists)
            val cardCreatePlaylist = dialogView.findViewById<MaterialCardView>(R.id.cardCreatePlaylist)
            val ivClose = dialogView.findViewById<ImageView>(R.id.ivCloseDialog)

            bottomSheetDialog.setContentView(dialogView)

            val adapter = PlaylistSelectAdapter(emptyList()) { playlist, isChecked ->
                if (currentSong != null) {
                    if (isChecked) {
                        addSongToPlaylist(playlist, currentSong)
                        Toast.makeText(this, "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        removeSongFromPlaylist(playlist, currentSong)
                        Toast.makeText(this, "Removed from ${playlist.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            val songId = currentSong?.musicId ?: ""

            // Step 1: Observe playlists
            playlistViewModel.allPlaylists.observe(this) { playlists ->
                adapter.clearSelection()
                adapter.updatePlaylists(playlists)

                // Step 2: Check which playlists contain the song

                val totalPlaylists = playlists.size
                val matchedPlaylistIds = mutableListOf<String>()
                var processed = 0

                playlists.forEach { playlist ->
                    val playlistId = playlist.playlistId ?: return@forEach

                    playlistViewModel.getSongsForPlaylist(playlistId) { songs ->
                        if (songs.any { it.songId == songId }) {
                            matchedPlaylistIds.add(playlistId)
                        }

                        processed++
                        if (processed == totalPlaylists) {
                            adapter.setSelectedPlaylists(matchedPlaylistIds)
                        }
                    }
                }


            }

            // Step 3: Handle Create Playlist
            cardCreatePlaylist.setOnClickListener {
                showCreatePlaylistDialog { newPlaylist ->
                    if (currentSong != null) {
                        addSongToPlaylist(newPlaylist, currentSong)
                        Toast.makeText(this, "Created and added to ${newPlaylist.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            ivClose.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        private fun showCreatePlaylistDialog(onPlaylistCreated: (PlaylistEntity) -> Unit) {
            val dialogView = layoutInflater.inflate(R.layout.playlist_create_dailog, null)

            val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.createPlaylist)
            val editText = inputLayout.editText
            val btnCreate = dialogView.findViewById<AppCompatButton>(R.id.createbtn)
            val btnCancel = dialogView.findViewById<AppCompatButton>(R.id.cancelbtn)

            val dialog = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            btnCreate.setOnClickListener {
                val name = editText?.text.toString().trim()
                if (name.isEmpty()) {
                    inputLayout.error = "Enter a name"
                } else {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        playlistViewModel.createPlaylist(name, userId)

                        // Observe once
                        playlistViewModel.allPlaylists.observe(this) { playlists ->
                            val created = playlists.find { it.name == name }
                            if (created != null) {
                                onPlaylistCreated(created)
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        private fun addSongToPlaylist(playlist: PlaylistEntity, song: UnifiedMusic) {
            val playlistId = playlist.playlistId ?: return

            playlistViewModel.getSongsForPlaylist(playlistId) { existingSongs ->
                val alreadyExists = existingSongs.any { it.songId == song.musicId }

                if (!alreadyExists) {
                    val playlistSong = PlaylistSongEntity(
                        playlistId = playlistId,
                        songId = song.musicId,
                        title = song.musicTitle,
                        artist = song.musicArtist,
                        album = song.musicAlbum,
                        image = song.imgUri,
                        audioUrl = song.musicPath,
                        duration = song.duration,
                        isLocal = song.isLocal
                    )

                    playlistViewModel.addSongsToPlaylist(listOf(playlistSong))
                } else {
                    Toast.makeText(this, "Song already in playlist", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun removeSongFromPlaylist(playlist: PlaylistEntity, song: UnifiedMusic) {
            playlistViewModel.removeSongsFromPlaylist(listOf(song), playlist.playlistId.toString())
            Toast.makeText(this, "Removed from ${playlist.name}", Toast.LENGTH_SHORT).show()
        }


        override fun finish() {
            super.finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
        }



        fun vibrate(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                ?: return

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(35)
            }
        }



    }


