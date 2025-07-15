package com.song.nafis.nf.TuneLyf.resource

object ValidationUtils {
    fun isValidEmail(email: String): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun isValidPassword(password: String): Boolean = password.length >= 6
}
