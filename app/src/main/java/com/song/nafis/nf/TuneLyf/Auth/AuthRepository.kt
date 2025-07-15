package com.song.nafis.nf.TuneLyf.Auth

import android.app.Activity
import android.net.Uri
import com.song.nafis.nf.TuneLyf.resource.Resource
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthCredential
import com.song.nafis.nf.TuneLyf.Model.Users
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun loginWithEmail(email: String, password: String): Flow<Resource<Unit>>
    fun registerWithEmail(email: String, password: String): Flow<Resource<Unit>>
    fun loginWithGoogle(credential: AuthCredential): Flow<Resource<Unit>>
    fun signInWithPhone(credential: PhoneAuthCredential): Flow<Resource<Unit>>
    fun sendOtp(phoneNumber: String, activity: Activity): Flow<Resource<String>>
    fun verifyOtp(verificationId: String, code: String): Flow<Resource<Boolean>>  // Return Resource<Unit> for OTP verification
    fun saveUserData(name:String="", phoneNumber: String="", email: String="", password: String=""): Flow<Resource<Unit>>
    fun updateUserData(name:String="", phoneNumber: String="", email: String="", password: String="",imgUrl:String?=""): Flow<Resource<Unit>>
    fun uploadProfileImageToStorage(imageUri: Uri): Flow<Resource<String>>
    fun deleteImageFromStorage(imageUrl: String): Flow<Resource<Unit>>
    fun logout()
    fun isUserLoggedIn(): Boolean
    fun getUserData(onResult: (Users?) -> Unit)
    fun forgotPassword(email: String): Flow<Resource<Unit>>
    fun getCurrentUserImgUrl(): String

}
