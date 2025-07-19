package com.song.nafis.nf.TuneLyf.Activity

import android.content.*
import android.content.pm.PackageManager
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline
import com.song.nafis.nf.TuneLyf.UI.AuthViewModel
import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
import com.song.nafis.nf.TuneLyf.databinding.ActivityAppSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class AppSetting : AppCompatActivity() {
    private lateinit var binding: ActivityAppSettingBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val musicViewModel: MusicViewModel by viewModels()
    private val viewModel: AuthViewModel by viewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSettingBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.settingroot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load theme before setContentView
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("dark_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Toolbar back button
        binding.settingtoolbar.setNavigationOnClickListener { onBackPressed() }
        val currentQuality = sharedPreferences.getString("audio_quality", "High")
        findViewById<TextView>(R.id.audioQualityValue).text = currentQuality
        // Version display
        val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionName
        }
        val appName = getString(R.string.app_name)
        binding.txtVersion.text = "$appName v$versionName"

        // Dark Theme Switch setup
        val themeSwitch =binding.switchTheme
        themeSwitch.isChecked = sharedPreferences.getBoolean("dark_mode", false)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Logout

    }

    fun onEqualizerClicked(view: View) {
        try {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Equalizer not supported on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    fun onAudioQualityClicked(view: View) {
        val qualities = arrayOf("Low", "Medium", "High")
        val currentQuality = sharedPreferences.getString("audio_quality", "High")
        val selectedIndex = qualities.indexOf(currentQuality)

        AlertDialog.Builder(this)
            .setTitle("Select Audio Quality")
            .setSingleChoiceItems(qualities, selectedIndex) { dialog, which ->
                val selected = qualities[which]
                sharedPreferences.edit().putString("audio_quality", selected).apply()
                findViewById<TextView>(R.id.audioQualityValue).text = selected
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun onAboutClicked(view: View) {
      startActivity(Intent(this, About::class.java))
    }
}
