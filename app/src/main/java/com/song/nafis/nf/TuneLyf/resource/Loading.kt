package com.song.nafis.nf.TuneLyf.resource

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.song.nafis.nf.TuneLyf.R

object Loading {

    private var loadingDialog: Dialog? = null

    fun show(context: Context) {
        if (loadingDialog?.isShowing == true) return

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loding, null)
        loadingDialog = Dialog(context).apply {
            setContentView(dialogView)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            try {
                if (context is Activity && !context.isFinishing && !context.isDestroyed) {
                    show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun hide() {
        try {
            val activity = loadingDialog?.context as? Activity
            if (loadingDialog?.isShowing == true &&
                activity != null &&
                !activity.isFinishing &&
                !activity.isDestroyed
            ) {
                loadingDialog?.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace() // avoid crash
        } finally {
            loadingDialog = null
        }
    }
}
