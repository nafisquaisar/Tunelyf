package com.song.nafis.nf.TuneLyf.Activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.MaterialToolbar

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    /**
     * Call this once with your root layout
     */
    protected fun applyEdgeToEdge(root: View) {
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )
                insets
            }
        }
    }

    protected fun applyStatusBarScrim(scrim: View) {
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(scrim) { view, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.layoutParams.height = top
                view.requestLayout()
                insets
            }
        } else {
            scrim.visibility = View.GONE
        }
    }
}
