package com.song.nafis.nf.TuneLyf.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicy : BaseActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Set up toolbar outside the insets listener
        setSupportActionBar(binding.appbar.toolbar)
        binding.appbar.toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Privacy Policy"
        }
    }
}
