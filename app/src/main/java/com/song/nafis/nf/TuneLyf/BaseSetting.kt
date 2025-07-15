package com.song.nafis.nf.TuneLyf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.song.nafis.nf.TuneLyf.databinding.ActivityBaseSettingBinding

class BaseSetting : AppCompatActivity() {
    private lateinit var binding: ActivityBaseSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityBaseSettingBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.baseSettingtoolbar.setNavigationOnClickListener { onBackPressed() }
    }
}