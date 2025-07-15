package com.song.nafis.nf.TuneLyf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.song.nafis.nf.TuneLyf.databinding.ActivityPlayNextOperationBinding

class PlayNextOperation : AppCompatActivity() {
    private lateinit var binding: ActivityPlayNextOperationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPlayNextOperationBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.playlisttoolbar.setNavigationOnClickListener { onBackPressed() }
    }
}