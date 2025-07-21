package com.song.nafis.nf.TuneLyf.resource

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.song.nafis.nf.TuneLyf.R
import timber.log.Timber

object Loading {

    private var loadingDialog: Dialog? = null

    fun show(context: Context) {
        val activity = context as? Activity
        if (activity == null || activity.isFinishing || activity.isDestroyed) return

        if (loadingDialog?.isShowing == true) return

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loding, null)
        val dialog = Dialog(context).apply {
            setContentView(dialogView)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        try {
            dialog.show()
            loadingDialog = dialog
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hide() {
        try {
            loadingDialog?.let { dialog ->
                if (dialog.isShowing) {
                    Timber.d("Hiding loading dialog")
                    dialog.dismiss()
                } else {
                    Timber.d("Loading dialog not showing")
                }
            }
        } catch (e: Exception) {
            Timber.e("Error hiding dialog: ${e.message}")
        } finally {
            loadingDialog = null
        }
    }

}
