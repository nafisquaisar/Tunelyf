package com.song.nafis.nf.TuneLyf.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.song.nafis.nf.TuneLyf.databinding.ActivityHelpBinding

class Help : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.helpRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 📧 Email click to open email client
        binding.emailText.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:tunelyfmusic@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Help Request - TuneLyf")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }

        // 📞 Phone click to open dialer
        binding.phoneText.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+919801999829")
            startActivity(intent)
        }

        setSupportActionBar(binding.helpToolbar)
        binding.helpToolbar.setNavigationOnClickListener { finish() }
    }
}
