    package com.song.nafis.nf.TuneLyf.UI

    import android.app.Activity
    import android.net.Uri
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.song.nafis.nf.TuneLyf.Auth.AuthRepository
    import com.google.firebase.auth.AuthCredential
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.PhoneAuthCredential
    import com.song.nafis.nf.TuneLyf.Model.Users
    import com.song.nafis.nf.TuneLyf.resource.AuthSuccess
    import com.song.nafis.nf.TuneLyf.resource.Resource
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.Flow
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await
    import javax.inject.Inject

    @HiltViewModel
    class AuthViewModel @Inject constructor(
        private val repository: AuthRepository
    ) : ViewModel() {

        private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

        var isGoogleSignIn = false
            private set

        var isNewUser = false
            private set

        // Make authState generic, so it can handle both Resource<Unit> and Resource<String>
        private var _authState = MutableStateFlow<Resource<Any>?>(null)
        val authState: StateFlow<Resource<Any>?> = _authState

        private val _otpState = MutableStateFlow<Resource<Boolean>?>(null)
        val otpState: StateFlow<Resource<Boolean>?> = _otpState

        private val _sendOtpState = MutableStateFlow<Resource<String>?>(null)
        val sendOtpState: StateFlow<Resource<String>?> = _sendOtpState

        private val _profileImageUrl = MutableStateFlow<String?>(null)
        val profileImageUrl: StateFlow<String?> = _profileImageUrl


        fun register(email: String, password: String) = viewModelScope.launch {
            repository.registerWithEmail(email, password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _authState.value = Resource.Success(AuthSuccess.Register)
                        isNewUser = true
                    }
                    is Resource.Loading -> _authState.value = Resource.Loading
                    is Resource.Error -> _authState.value = Resource.Error(result.message)
                    else -> Unit
                }
            }
        }

        fun login(email: String, password: String) = viewModelScope.launch {
            repository.loginWithEmail(email, password).collect { result ->
                when (result) {
                    is Resource.Success -> _authState.value = Resource.Success(AuthSuccess.Login)
                    is Resource.Loading -> _authState.value = Resource.Loading
                    is Resource.Error -> _authState.value = Resource.Error(result.message)
                    else -> Unit
                }
            }
        }

        fun googleSignIn(credential: AuthCredential) = viewModelScope.launch {
            isGoogleSignIn = true
                _authState.value = Resource.Loading // Add this line
            try {
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                isNewUser = authResult.additionalUserInfo?.isNewUser == true
                _authState.value = Resource.Success(AuthSuccess.Login)
            } catch (e: Exception) {
                _authState.value = Resource.Error(e.message)
            }
        }








        fun phoneSignIn(credential: PhoneAuthCredential) = viewModelScope.launch {
            repository.signInWithPhone(credential).collect { _authState.value = it }
        }

        fun logout() = repository.logout()


        // Send OTP for phone verification
        fun sendOtp(phone: String, activity: Activity) = viewModelScope.launch {
            repository.sendOtp(phone, activity).collect { _sendOtpState.value = it }
        }

        fun verifyOtp(verificationId: String, code: String) = viewModelScope.launch {
            repository.verifyOtp(verificationId, code).collect { _otpState.value = it }
        }



        fun saveUserData(name: String = "", phoneNumber: String = "", email: String = "", password: String = "") =
            viewModelScope.launch {
                repository.saveUserData(name, phoneNumber, email, password).collect {
                    _authState.value = it
                }
            }

        fun updateUserDetails(name: String, phone: String, email: String, password: String, imgUrl: String?): Flow<Resource<Unit>> {
            return repository.updateUserData(name, phone, email, password, imgUrl)
        }

        fun getCurrentUserImageUrl(): String {
            return repository.getCurrentUserImgUrl()
        }



        fun uploadProfilePhoto(imgUrl: Uri) = viewModelScope.launch {
            _authState.value = Resource.Loading // Notify UI to show progress

            repository.uploadProfileImageToStorage(imgUrl).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _authState.value = Resource.Loading // Optional, already set above
                    }
                    is Resource.Success -> {
                        _authState.value = Resource.Success(result.data ?: "Image uploaded successfully")
                    }
                    is Resource.Error -> {
                        _authState.value = Resource.Error(result.message)
                    }
                }
            }
        }


        fun fetchUserData(onResult: (Users?) -> Unit) {
            repository.getUserData { user ->
                onResult(user)
            }
        }

        fun isLogin(): Boolean {
            return repository.isUserLoggedIn()
        }

        fun forgotPassword(email: String) = viewModelScope.launch {
            repository.forgotPassword(email).collect { result ->
                when (result) {
                    is Resource.Success -> _authState.value = Resource.Success(AuthSuccess.ForgotPassword)
                    is Resource.Loading -> _authState.value = Resource.Loading
                    is Resource.Error -> _authState.value = Resource.Error(result.message)
                    else -> Unit
                }
            }
        }

        fun deleteImageFromStorage(imageUrl: String) {
            repository.deleteImageFromStorage(imageUrl)
        }

    }
