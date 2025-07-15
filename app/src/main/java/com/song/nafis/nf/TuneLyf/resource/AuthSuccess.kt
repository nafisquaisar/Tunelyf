package com.song.nafis.nf.TuneLyf.resource

sealed class AuthSuccess {
    object Login : AuthSuccess()
    object Register : AuthSuccess()
    object ForgotPassword : AuthSuccess()
    data class Other(val message: String = "") : AuthSuccess()
}
