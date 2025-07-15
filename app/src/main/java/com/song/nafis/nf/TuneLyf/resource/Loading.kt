package com.song.nafis.nf.TuneLyf.resource

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
        }
        loadingDialog?.show()
    }

    fun hide() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}
