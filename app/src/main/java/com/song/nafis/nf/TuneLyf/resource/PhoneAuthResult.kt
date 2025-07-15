package com.song.nafis.nf.TuneLyf.resource

sealed class PhoneAuthResult {
    data class CodeSent(val verificationId: String) : PhoneAuthResult()
    object Verified : PhoneAuthResult()
    data class Error(val message: String?) : PhoneAuthResult()
}
