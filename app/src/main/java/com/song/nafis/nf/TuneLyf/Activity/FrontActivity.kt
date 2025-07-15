package com.song.nafis.nf.TuneLyf.Activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.song.nafis.nf.TuneLyf.databinding.ActivityFrontBinding

class FrontActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFrontBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFrontBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root) // ✅ Use the inflated view

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnGetStarted.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.signInLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
