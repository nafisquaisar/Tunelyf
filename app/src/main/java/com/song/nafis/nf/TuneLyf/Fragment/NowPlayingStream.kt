    package com.song.nafis.nf.TuneLyf.Fragment

    import android.animation.ArgbEvaluator
    import android.animation.ValueAnimator
    import android.content.Intent
    import android.graphics.Color
    import android.graphics.drawable.ColorDrawable
    import android.graphics.drawable.GradientDrawable
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import com.bumptech.glide.Glide
    import com.song.nafis.nf.TuneLyf.Activity.PlayMusicStreamActivity
    import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
    import com.song.nafis.nf.TuneLyf.databinding.FragmentNowPlayingStreamBinding
    import com.song.nafis.nf.TuneLyf.R
    import timber.log.Timber
    import kotlin.random.Random

    class NowPlayingStream : Fragment() {

        private var _binding: FragmentNowPlayingStreamBinding? = null
        private val binding get() = _binding!!

        private val viewModel: MusicViewModel by activityViewModels()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentNowPlayingStreamBinding.inflate(inflater, container, false)
            return binding.root
        }

        private var colorAnimator: ValueAnimator? = null // add at class level

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            Timber.d("🎶 NowPlayingStream loaded")

            viewModel.refreshNowPlayingUI()
            observeNowPlaying()
            observeSeekBar()
            setupClickListeners()
        }

        private fun observeNowPlaying() {
            viewModel.currentSongTitle.observe(viewLifecycleOwner) { title ->
                Timber.d("🎯 Title LiveData: $title")
                binding.musicTitle.text = title
                // 🎨 Change background dynamically on song change
                animateNowPlayingBackground()
            }


            viewModel.currentSongArtwork.observe(viewLifecycleOwner) { url ->
                Timber.d("🖼️ Artwork LiveData: $url")
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.music_img)
                    .into(binding.musicImage)
            }


            viewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
                val icon = if (playing) R.drawable.pause_notification else R.drawable.play_notificaiton
                binding.playPauseBtn.setImageResource(icon)
            }

        }

        private fun observeSeekBar() {
            val updateProgress = {
                val duration = viewModel.currentDurationMillis.value ?: 0L
                val position = viewModel.currentPositionMillis.value ?: 0L
                if (duration > 0) {
                    val progress = (position * 100 / duration).toInt()
                    binding.seekBar.progress = progress
                }
            }

            viewModel.currentPositionMillis.observe(viewLifecycleOwner) { updateProgress() }
            viewModel.currentDurationMillis.observe(viewLifecycleOwner) { updateProgress() }

            // ✅ Single observe, not nested
            viewModel.isBuffering.observe(viewLifecycleOwner) { buffering ->
                if (buffering) {
                    binding.playPauseBtn.visibility = View.GONE
                    binding.loadingContainer.visibility = View.VISIBLE
                } else {
                    binding.loadingContainer.visibility = View.GONE
                    binding.playPauseBtn.visibility = View.VISIBLE
                }
            }
        }


        private fun setupClickListeners() {
            binding.playPauseBtn.setOnClickListener {
                viewModel.playPauseToggle(requireContext())
            }
            binding.nextBtn.setOnClickListener {
                viewModel.nextSong()
            }
            binding.prevBtn.setOnClickListener {
                viewModel.previousSong()
            }


            binding.root.setOnClickListener {
                val context = requireContext()

                val songTitle = viewModel.currentSongTitle.value ?: "Unknown"
                val songArtwork = viewModel.currentSongArtwork.value ?: ""
                val songList = viewModel.playlistLiveData.value ?: emptyList()
                val currentSong = viewModel.currentUnifiedSong.value

                if (currentSong == null || songList.isEmpty()) return@setOnClickListener

                val currentIndex = songList.indexOfFirst { it.musicId == currentSong.musicId }

                if (currentIndex == -1) return@setOnClickListener

                val intent = Intent(context, PlayMusicStreamActivity::class.java).apply {
                    putExtra("SONG_TITLE", songTitle)
                    putExtra("SONG_TRACK", songArtwork)
                    putParcelableArrayListExtra("SONG_LIST", ArrayList(songList))
                    putExtra("SONG_INDEX", currentIndex)
                }

                context.startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
            }


        }

        override fun onDestroyView() {
            super.onDestroyView()
            colorAnimator?.cancel()
            _binding = null
        }


        override fun onResume() {
            super.onResume()
        }


        fun getRandomOpaqueDarkColor(): Int {
            val random = Random(System.currentTimeMillis())

            while (true) {
                val r = random.nextInt(0, 100)   // keep red low
                val g = random.nextInt(0, 150)
                val b = random.nextInt(0, 150)

                val brightness = r + g + b
                val isNotPinkish = !(r > g + 30 && r > b + 30) // pink/red tone check

                if (brightness < 250 && isNotPinkish) {
                    val alpha = 120 // semi-transparent
                    return Color.argb(alpha, r, g, b)
                }
            }
        }



        private fun animateNowPlayingBackground() {
            val drawable = binding.nowPlayingRoot.background as? GradientDrawable ?: return
            val colorFrom = (drawable.color?.defaultColor ?: Color.TRANSPARENT)
            val colorTo = getRandomOpaqueDarkColor()

            colorAnimator?.cancel()
            colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
                duration = 500
                addUpdateListener { animator ->
                    val newColor = animator.animatedValue as Int
                    drawable.setColor(newColor)
                }
                start()
            }
        }

    }
