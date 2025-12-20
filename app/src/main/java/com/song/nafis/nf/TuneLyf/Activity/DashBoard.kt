    package com.song.nafis.nf.TuneLyf.Activity

    import com.song.nafis.nf.TuneLyf.R
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.os.Build
    import android.os.Bundle
    import android.os.Handler
    import android.os.Looper
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.Menu
    import android.view.View
    import android.widget.FrameLayout
    import android.widget.Toast
    import androidx.activity.viewModels
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.widget.SearchView
    import androidx.appcompat.widget.Toolbar
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.fragment.app.Fragment
    import com.bumptech.glide.Glide
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import com.song.nafis.nf.TuneLyf.Activity.AppSetting
    import com.song.nafis.nf.TuneLyf.BaseSetting
    import com.song.nafis.nf.TuneLyf.Fragment.HomeFragment
    import com.song.nafis.nf.TuneLyf.Fragment.MusicSearchFragment
    import com.song.nafis.nf.TuneLyf.Fragment.NowPlayingStream
    import com.song.nafis.nf.TuneLyf.Fragment.PlayListFragment
    import com.song.nafis.nf.TuneLyf.Fragment.ProfileFragment

    //import com.song.nafis.nf.blissfulvibes.PlaymusicList
    import com.song.nafis.nf.TuneLyf.UI.AuthViewModel
    import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
    import com.song.nafis.nf.TuneLyf.databinding.ActivityDashBoardBinding
    import dagger.hilt.android.AndroidEntryPoint
    import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle
    import timber.log.Timber
    import android.Manifest
    import android.graphics.Color
    import android.widget.ImageButton
    import androidx.activity.enableEdgeToEdge
    import androidx.cardview.widget.CardView
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.core.view.WindowInsetsControllerCompat
    import com.song.nafis.nf.TuneLyf.FavoriteMusic
    import com.song.nafis.nf.TuneLyf.MainActivity
    import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline


    @AndroidEntryPoint
    class DashBoard : AppCompatActivity() {
        lateinit var binding: ActivityDashBoardBinding
        private lateinit var toolbar: Toolbar
        private lateinit var drawerToggle: DuoDrawerToggle

        private val viewModel: AuthViewModel by viewModels()
        private val musicViewModel: MusicViewModel by viewModels()

        private val handler = Handler(Looper.getMainLooper())
        private var hideRunnable: Runnable? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            binding= ActivityDashBoardBinding.inflate(LayoutInflater.from(this))
            setContentView(binding.root)



            ViewCompat.setOnApplyWindowInsetsListener(binding.homecontent) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.isAppearanceLightStatusBars = false





            toolbar = binding.hometoolbar
            setSupportActionBar(toolbar)

//            messageForDelayDialog()
            clickbtn()
            drawer()
            setprofile()
            permissionFun()
            setBottomNavigation(savedInstanceState)



//            musicViewModel.isPlaying.observe(this) { playing ->
//                Timber.tag("dashboard").d("🔁 isPlaying changed: $playing")
//
//                val container = findViewById<FrameLayout>(R.id.nowPlayingContainer)
//
//                if (playing) {
//                    // Cancel any pending hide tasks
//                    hideRunnable?.let { handler.removeCallbacks(it) }
//
//                    // Show fragment if not already shown
//                    if (supportFragmentManager.findFragmentById(R.id.nowPlayingContainer) == null) {
//                        supportFragmentManager.beginTransaction()
//                            .replace(R.id.nowPlayingContainer, NowPlayingStream())
//                            .commit()
//                    }
//
//                    container.visibility = View.VISIBLE
//                } else {
//                    // Schedule hiding after 20 minutes (20 * 60 * 1000 ms)
//                    hideRunnable = Runnable {
//                        container.visibility = View.GONE
//                        Timber.tag("dashboard").d("⏲️ Hiding NowPlaying after 20 mins of pause.")
//                    }
//                    handler.postDelayed(hideRunnable!!, 20 * 60 * 1000L)
//                }
//            }


            versionSet()
        musicViewModel.isPlaying.observe(this) { updateNowPlayingVisibility() }
        musicViewModel.isBuffering.observe(this) { updateNowPlayingVisibility() }
        }


        fun updateNowPlayingVisibility() {
            val playing = musicViewModel.isPlaying.value ?: false
            val buffering = musicViewModel.isBuffering.value ?: false
            val container = findViewById<FrameLayout>(R.id.nowPlayingContainer)

            Timber.tag("dashboard").d("🔁 isPlaying: $playing | 📡 isBuffering: $buffering")

            if (playing || buffering) {
                hideRunnable?.let { handler.removeCallbacks(it) }

                // Load fragment if needed
                if (supportFragmentManager.findFragmentById(R.id.nowPlayingContainer) == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nowPlayingContainer, NowPlayingStream())
                        .commit()
                }

                container.visibility = View.VISIBLE
            } else {
                // Delay hiding after 20 minutes of inactivity
                hideRunnable = Runnable {
                    container.visibility = View.GONE
                    Timber.tag("dashboard").d("⏲️ Hiding NowPlaying after 20 mins of pause.")
                }
                handler.postDelayed(hideRunnable!!, 20 * 60 * 1000L)
            }
        }


//        private fun messageForDelayDialog() {
//            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
//            val shown = prefs.getBoolean("audius_first_time_msg_shown", false)
//
//            if (!shown) {
//                val dialogView = layoutInflater.inflate(R.layout.info_message, null)
//                val dialog = android.app.AlertDialog.Builder(this)
//                    .setView(dialogView)
//                    .setCancelable(false)
//                    .create()
//
//                val closeBtn = dialogView.findViewById<ImageButton>(R.id.closeInfo)
//                closeBtn.setOnClickListener {
//                    dialog.dismiss()
//                    prefs.edit().putBoolean("audius_first_time_msg_shown", true).apply()
//                }
//
//                dialog.show()
//
//                // 💡 This removes extra square corners
//                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//            }
//        }


        private fun versionSet() {
            val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionName
            }
            val appName = getString(R.string.app_name)
            binding.drawerVersion.text = "$appName v$versionName"

        }

        private fun permissionFun() {
            val permissionList = mutableListOf<String>()

            // For Android 13+ (TIRAMISU)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Notification Permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(Manifest.permission.POST_NOTIFICATIONS)
                }

                // Read Media Audio Permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(Manifest.permission.READ_MEDIA_AUDIO)
                }
            }

            // Request if any permission is not granted
            if (permissionList.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionList.toTypedArray(),
                    101
                )
            }
        }



        private fun setBottomNavigation(savedInstanceState: Bundle?) {
            binding.BottomNavigationBar.setOnItemSelectedListener { item ->
                val id = item.itemId
                val preferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
                val editor = preferences.edit()

                when (id) {
                    R.id.nav_home -> {
                        loadfragment(HomeFragment(), 0)
                        editor.putString("LAST_FRAGMENT", "Home")
                    }
                    R.id.nav_music_search -> {
                        loadfragment(MusicSearchFragment(), 1)
                        editor.putString("LAST_FRAGMENT", "search")
                    }
    //                R.id.nav_Shuffle -> {
    //                    startActivity(Intent(this@DashBoard, PlaymusicList::class.java))
    //                }
                    R.id.nav_mymusic -> {
                        loadfragment(PlayListFragment(), 1)
                        editor.putString("LAST_FRAGMENT", "playlist")
                    }
                    R.id.nav_profile -> {
                        loadfragment(ProfileFragment(), 1)
                        editor.putString("LAST_FRAGMENT", "Profile")
                    }
                }
                editor.apply()
                true
            }

            val preferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
            val lastFragment = preferences.getString("LAST_FRAGMENT", "Home")

            if (savedInstanceState == null) {
                // If the app is opened for the first time, load Home
                binding.BottomNavigationBar.selectedItemId = R.id.nav_home
            } else {
                // If returning from another activity, restore the last fragment
                when (lastFragment) {
                    "Home" -> binding.BottomNavigationBar.selectedItemId = R.id.nav_home
                    "search" -> binding.BottomNavigationBar.selectedItemId = R.id.nav_music_search
                    "playlist" -> binding.BottomNavigationBar.selectedItemId = R.id.nav_mymusic
                    "Profile" -> binding.BottomNavigationBar.selectedItemId = R.id.nav_profile
                }
            }

        }

        private fun setprofile() {
            var shimmerLayout=binding.proShimmer.root


            shimmerLayout.startShimmer()
            shimmerLayout.visibility = View.VISIBLE
            binding.profileHeader.visibility = View.GONE



            viewModel.fetchUserData { user ->
                if (user != null) {
                    binding.proName.text = user.name.ifEmpty { "Blissful Vibes" }
                    binding.proEmail.text=user.email.ifEmpty{""}

                    if (user.imgUrl.isNotEmpty()) {
                        if (!isDestroyed && !isFinishing) {
                        Glide.with(this)
                            .load(user.imgUrl)
                            .placeholder(R.drawable.profileicon) // Optional placeholder while loading
                            .error(R.drawable.profileicon)       // Optional fallback on error
                            .into(binding.profileImg)
                        }
                    } else {
                        binding.profileImg.setImageResource(R.drawable.profileicon)
                    }
                    // After data loads
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    binding.profileHeader.visibility = View.VISIBLE
                } else {
                    binding.proName.text = "Blissful Vibes"
                    binding.profileImg.setImageResource(R.drawable.profileicon)
                    binding.proEmail.text=""
                    // After data loads
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    binding.profileHeader.visibility = View.VISIBLE
                }
            }

        }

        // 2. Handle activity resume properly
        override fun onResume() {
            super.onResume()

            if (!isFinishing && !isDestroyed) {
                setprofile()
                musicViewModel.observePlayer() // 🔁 Add this
            }
        }


        private fun clickbtn() {
            binding.apply {

                drawermymusic.setOnClickListener {
                    startActivity(Intent(this@DashBoard, MainActivity::class.java))
                }
                llSetting.setOnClickListener {
                    startActivity(Intent(this@DashBoard, AppSetting::class.java))
                }
                DrawerFavorites.setOnClickListener {
                    startActivity(Intent(this@DashBoard, FavoriteMusic::class.java))
                }

                DrawerHelp.setOnClickListener {
                    startActivity(Intent(this@DashBoard, Help::class.java))
                }

                DrawerAbout.setOnClickListener {
                    startActivity(Intent(this@DashBoard, About::class.java))
                }

                DrawerPrivacy.setOnClickListener {
                    startActivity(Intent(this@DashBoard, PrivacyPolicy::class.java))
                }
                DrawerRecentPlay.setOnClickListener {
                    startActivity(Intent(this@DashBoard, RecentPlayList::class.java))
                }
                llExit.setOnClickListener {
                    val builder = MaterialAlertDialogBuilder(this@DashBoard)
                        .setTitle("LogOut")
                        .setMessage("Are you Sure to LogOut")
                        .setPositiveButton("Yes") { _, _ ->
                            // Stop the music service
                            val stopIntent = Intent(this@DashBoard, MusicServiceOnline::class.java)
                            this@DashBoard.stopService(stopIntent)

    // Stop playback in repository
                            musicViewModel.cancelStopTimer()
                            musicViewModel.playerRepository.stopCurrentSong()
    //                        MusicServiceOnline.isServiceStopped = true
                            musicViewModel.isPlaying.postValue(false)

                            viewModel.logout()
                            val intent = Intent(this@DashBoard, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }

                    val alertDialog = builder.create()
                    alertDialog.show()
                    val color = ContextCompat.getColor(this@DashBoard, R.color.icon_color)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color)
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color)
                }
            }
        }

        private fun drawer() {

            val drawerLayout = binding.drawer
            drawerToggle = object : DuoDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            ) {
                override fun onDrawerOpened(drawerView: android.view.View) {
                    super.onDrawerOpened(drawerView)
                    // Change to back arrow and tint it white
                    toolbar.navigationIcon = ContextCompat.getDrawable(this@DashBoard, R.drawable.back_arrow)?.mutate()
                    toolbar.navigationIcon?.setTint(ContextCompat.getColor(this@DashBoard, R.color.alwayswhite))
                }

                override fun onDrawerClosed(drawerView: android.view.View) {
                    super.onDrawerClosed(drawerView)
                    // Change to hamburger icon and tint it white
                    toolbar.navigationIcon = ContextCompat.getDrawable(this@DashBoard, R.drawable.drawer_icon)?.mutate()
                    toolbar.navigationIcon?.setTint(ContextCompat.getColor(this@DashBoard, R.color.alwayswhite))
                }
            }

            drawerLayout.setDrawerListener(drawerToggle)
            drawerToggle.syncState()
            // ✅ Set white color to the initial navigation icon here
            toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.drawer_icon)?.mutate()
            toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.alwayswhite))

        }




        //  ***************** Fragment Load Function *****************************
        fun loadfragment(fragment: Fragment, flag:Int){
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            if (flag == 0) {
                fragmentTransaction.add(R.id.container, fragment)
            } else {
                fragmentTransaction.replace(R.id.container, fragment)
            }
            fragmentTransaction.commit()
        }

        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.playlist_menu, menu)
            val searchView = menu?.findItem(R.id.search_playlist)?.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    Log.d("MainActivity", "onQueryTextSubmit: $query")
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d("MainActivity", "onQueryTextChange: $newText")
                    Toast.makeText(this@DashBoard, newText.toString(), Toast.LENGTH_SHORT).show()
    //                musiclistSerach = ArrayList()
    //                if (newText != null) {
    //                    val userInput = newText.lowercase()
    //                    for (song in musiclist) {
    //                        if (song.musicTitle.lowercase().contains(userInput)) {
    //                            musiclistSerach.add(song)
    //                        }
    //                    }
    //                    search = true
    //                }
                    return true
                }
            })
            return super.onCreateOptionsMenu(menu)
        }




    }