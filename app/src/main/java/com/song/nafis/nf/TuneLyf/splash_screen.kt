package com.song.nafis.nf.TuneLyf

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.song.nafis.nf.TuneLyf.Activity.DashBoard
import com.song.nafis.nf.TuneLyf.Activity.FrontActivity
import com.song.nafis.nf.TuneLyf.UI.AuthViewModel
import com.song.nafis.nf.TuneLyf.UI.PlaylistViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class splash_screen : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            val isLogin = viewModel.isLogin()
            val intent = if (isLogin) {
                Intent(this, DashBoard::class.java)
            } else {
                Intent(this, FrontActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000)
 
        // ******* Sync the Playlist From firebase and store to the local database ******
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        userId?.let {
//            playlistViewModel.syncPlaylistsFromFirebase(it)
//        }


        val lottieView = findViewById<LottieAnimationView>(R.id.splash_animation)

        lottieView.setAnimation(R.raw.musicanimation) // without .json
        lottieView.repeatCount = LottieDrawable.INFINITE
        lottieView.playAnimation()


    }
}
