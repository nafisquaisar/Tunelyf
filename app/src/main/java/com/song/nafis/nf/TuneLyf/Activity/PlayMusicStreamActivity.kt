    package com.song.nafis.nf.TuneLyf.Activity

    import android.content.Context
    import android.content.Intent
    import android.media.AudioManager
    import android.media.audiofx.AudioEffect
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
    import android.widget.ImageView
    import android.widget.LinearLayout
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.widget.AppCompatButton
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
    import com.song.nafis.nf.TuneLyf.UI.FavoriteViewModel
    import com.song.nafis.nf.TuneLyf.UI.PlaylistViewModel
    import com.song.nafis.nf.TuneLyf.UI.RecentlyPlayedViewModel
    import com.song.nafis.nf.TuneLyf.adapter.PlaylistSelectAdapter

    @AndroidEntryPoint
    class PlayMusicStreamActivity : AppCompatActivity() {

        private lateinit var binding: ActivityPlayMusicStreamBinding
        // With this:
        private val viewModel: MusicViewModel by viewModels()
        private val playlistViewModel: PlaylistViewModel by viewModels()
        private val favoriteViewModel: FavoriteViewModel by viewModels()
        private val recentlyPlayedViewModel: RecentlyPlayedViewModel by viewModels()

        private var stopMusicHandler: Handler? = null
        private var stopRunnable: Runnable? = null


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityPlayMusicStreamBinding.inflate(layoutInflater)
            setContentView(binding.root)

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

            if (!isSamePlaylist || !isSameSong) {
                viewModel.setPlaylist(songList)
                viewModel.setInitialIndex(currentIndex,this)
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

        }

        private fun setupUI(songArtwork: String, songTitle: String) {
            binding.playMusicTitle.isSelected = true

            binding.playMusicplaypausebtn.setOnClickListener {
                viewModel.playPauseToggle(this)
            }
            binding.playMusicNextbtn.setOnClickListener {
                Timber.tag("test").d("Next button clicked")
                viewModel.nextSong()
            }
            binding.playMusicPreviousbtn.setOnClickListener {
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
                val songTitle = viewModel.currentSongTitle.value ?: "My Favorite Song"
                val currentAudio = viewModel.playMusicStatus.value
                var songUrl = ""

                if (currentAudio is Resource.Success) {
                    songUrl = currentAudio.data.first
                }

                if (songUrl.isNotBlank()) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "$songTitle - Listen now: $songUrl")
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share song via"))
                } else {
                    Toast.makeText(this, "No song is playing", Toast.LENGTH_SHORT).show()
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


    // 🔥 Set the listener after ViewModel is ready

            Glide.with(this)
                .load(songArtwork)
                .placeholder(R.mipmap.music_icon)
                .into(binding.playmusicImg)

            binding.playMusicSeek.max = 0
            binding.musicEndTime.text = "00:00"
            binding.musicTimeStart.text = "00:00"

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
                if (!viewModel.isBuffering.value!!) {
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
                        val currentlyPlayingId = viewModel.currentUnifiedSong.value?.musicId
                        if (currentlyPlayingId != song.musicId) {
                            viewModel.startMusicService(this, song)
                        }
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


    }


