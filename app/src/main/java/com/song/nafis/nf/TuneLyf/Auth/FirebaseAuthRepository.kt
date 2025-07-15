package com.song.nafis.nf.TuneLyf.Auth


import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.song.nafis.nf.TuneLyf.Model.Users
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.resource.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.util.concurrent.TimeUnit


class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) : AuthRepository {

    var lastFetchedUser: Users? = null

    override fun loginWithEmail(email: String, password: String): Flow<Resource<Unit>> = callbackFlow {
        trySend(Resource.Loading)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
            }
            .addOnFailureListener { exception ->
                val message = when (exception) {
                    is FirebaseAuthInvalidUserException -> "Email not registered"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid credentials. Check your password."
                    else -> exception.localizedMessage ?: "Login failed"
                }
                trySend(Resource.Error(message))
            }

        awaitClose()
    }



    override fun registerWithEmail(email: String, password: String): Flow<Resource<Unit>> = callbackFlow {
        trySend(Resource.Loading)
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { trySend(Resource.Success(Unit)) }
            .addOnFailureListener { trySend(Resource.Error(it.message)) }

        awaitClose()
    }

    override fun loginWithGoogle(credential: AuthCredential): Flow<Resource<Unit>> = callbackFlow {
        trySend(Resource.Loading)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))

            }
            .addOnFailureListener { trySend(Resource.Error(it.message)) }

        awaitClose()
    }



    override fun signInWithPhone(credential: PhoneAuthCredential): Flow<Resource<Unit>> = callbackFlow {
        trySend(Resource.Loading)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { trySend(Resource.Success(Unit)) }
            .addOnFailureListener { trySend(Resource.Error(it.message)) }

        awaitClose()
    }


    override fun sendOtp(phoneNumber: String, activity: Activity): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        trySend(Resource.Success("Auto verification completed"))
                    }
                    .addOnFailureListener { e ->
                        trySend(Resource.Error(e.message ?: "Unknown error"))
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                trySend(Resource.Error(e.message ?: "Verification failed"))
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                trySend(Resource.Success(verificationId))
            }
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            activity,
            callbacks
        )

        awaitClose { /* cleanup if necessary */ }
    }
    override fun verifyOtp(verificationId: String, code: String): Flow<Resource<Boolean>> = callbackFlow {
        trySend(Resource.Loading)
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { trySend(Resource.Success(true)) }  // ✅ change here
            .addOnFailureListener { trySend(Resource.Error(it.message)) }

        awaitClose()
    }


    override fun logout() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            return
        }

        // Determine the provider for the current user
        var provider = ""
        for (profile in currentUser.providerData) {
            when (profile.providerId) {
                "google.com" -> provider = "Google"
                "password" -> provider = "EmailPassword"
                "phone" -> provider = "Phone"
            }
        }

        // Handle Google sign-out and Firebase sign-out
        when (provider) {
            "Google" -> {
                // Sign out from Google
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id)) // Get this from google-services.json
                    .requestEmail()
                    .build()

                val googleSignInClient = GoogleSignIn.getClient(context, gso)

                // Sign out from Google
                googleSignInClient.signOut().addOnCompleteListener {
                    // Sign out from Firebase as well
                    firebaseAuth.signOut()

                    // Optionally notify UI about the sign-out
                    // You could trigger a LiveData update or a callback to inform the UI
                    Log.d("AuthRepository", "Logged out from Google")
                }
            }
            "EmailPassword" -> {
                // Sign out from Firebase for Email/Password provider
                firebaseAuth.signOut()

                // Optionally notify UI about the sign-out
                Log.d("AuthRepository", "Logged out from Email/Password")
            }
            "Phone" -> {
                // Handle phone authentication if needed (Usually, Firebase sign-out is enough)
                firebaseAuth.signOut()

                // Optionally notify UI about the sign-out
                Log.d("AuthRepository", "Logged out from Phone")
            }
        }

        // Optionally, you can clear any session data or reset UI state here
    }


    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun saveUserData(
        name: String,
        phoneNumber: String,
        email: String,
        password: String
    ): Flow<Resource<Unit>> = callbackFlow {
        trySend(Resource.Loading)

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not logged in"))
            awaitClose()
            return@callbackFlow
        }

        val userId = currentUser.uid
        val timestamp = System.currentTimeMillis().toString()

        // Determine provider
        var provider = ""
        for (profile in currentUser.providerData) {
            when (profile.providerId) {
                "google.com" -> provider = "Google"
                "password" -> provider = "EmailPassword"
                "phone" -> provider = "Phone"
            }

        }

        val finalName = if (provider == "Google") {
            currentUser.email?.substringBefore("@") ?: email.substringBefore("@")
        } else {
            email.substringBefore("@")
        }

        val modifiedUserId = "$userId-${finalName.trim().replace("\\s+".toRegex(), "_")}"

        val finalUser = when (provider) {
            "Google" -> Users(
                userId = userId,
                name = name.ifEmpty { currentUser.displayName ?: "" },
                phone = "",
                email = email.ifEmpty { currentUser.email ?: "" },
                password = "",
                timestamp = timestamp,
                imgUrl = currentUser.photoUrl.toString()
            )

            "Phone" -> Users(
                userId = userId,
                name = name,
                phone = phoneNumber.ifEmpty { currentUser.phoneNumber ?: "" },
                email = email,
                password = "",
                timestamp = timestamp,
                imgUrl = ""
            )

            "EmailPassword" -> Users(
                userId = userId,
                name = name,
                phone = phoneNumber,
                email = email,
                password = password,  // Never store password
                timestamp = timestamp,
                imgUrl = ""
            )

            else -> Users()
        }

        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(modifiedUserId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    userRef.setValue(finalUser)
                        .addOnSuccessListener {
                            trySend(Resource.Success(Unit))
                        }
                        .addOnFailureListener {
                            trySend(Resource.Error(it.message))
                        }
                } else {
                    trySend(Resource.Success(Unit)) // Already saved
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        })

        awaitClose()
    }

    override fun updateUserData(
        name: String,
        phoneNumber: String,
        email: String,
        password: String,
        imgUrl:String?
    ): Flow<Resource<Unit>> = callbackFlow {
        trySend(Resource.Loading)

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not logged in"))
            awaitClose()
            return@callbackFlow
        }

        val userId = currentUser.uid
        val timestamp = System.currentTimeMillis().toString()

        // Determine provider
        var provider = ""
        for (profile in currentUser.providerData) {
            when (profile.providerId) {
                "google.com" -> provider = "Google"
                "password" -> provider = "EmailPassword"
                "phone" -> provider = "Phone"
            }
        }

        val finalName = if (provider == "Google") {
            currentUser.email?.substringBefore("@") ?: email.substringBefore("@")
        } else {
            email.substringBefore("@")
        }

        val modifiedUserId = "$userId-${finalName.trim().replace("\\s+".toRegex(), "_")}"
        val finalImgUrl = imgUrl ?: currentUser.photoUrl?.toString() ?: ""
        val updatedUser = when (provider) {
            "Google" -> Users(
                userId = userId,
                name = name.ifEmpty { currentUser.displayName ?: "" },
                phone = phoneNumber.ifEmpty { currentUser.phoneNumber ?: "" }, // not typically available from Google
                email = email.ifEmpty { currentUser.email ?: "" },
                password = "", // avoid storing passwords
                timestamp = timestamp,
                imgUrl = finalImgUrl
            )

            "Phone" -> Users(
                userId = userId,
                name = name,
                phone = phoneNumber.ifEmpty { currentUser.phoneNumber ?: "" },
                email = email,
                password = "",
                timestamp = timestamp,
                imgUrl = finalImgUrl
            )

            "EmailPassword" -> Users(
                userId = userId,
                name = name,
                phone = phoneNumber,
                email = email,
                password = password,
                timestamp = timestamp,
                imgUrl = finalImgUrl
            )

            else -> Users()
        }

        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(modifiedUserId)

        userRef.setValue(updatedUser)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.message))
            }

        awaitClose()
    }

    override fun uploadProfileImageToStorage(imageUri: Uri): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading)

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error("User not logged in"))
            awaitClose()
            return@callbackFlow
        }

        val emailPrefix = currentUser.email?.substringBefore("@")?.trim()?.replace("\\s+".toRegex(), "_")
        val userId = currentUser.uid
        val modifiedUserId = if (!emailPrefix.isNullOrEmpty()) {
            "$userId-$emailPrefix"
        } else {
            userId // fallback
        }
        val imageRef = FirebaseStorage.getInstance().getReference("ProfileImages/$modifiedUserId.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    trySend(Resource.Success(uri.toString()))
                }.addOnFailureListener { e ->
                    trySend(Resource.Error(e.message ?: "Failed to retrieve image URL"))
                }
            }
            .addOnFailureListener { e ->
                trySend(Resource.Error(e.message ?: "Image upload failed"))
            }

        awaitClose()
    }

    override fun deleteImageFromStorage(imageUrl: String): Flow<Resource<Unit>> = callbackFlow {
        try {
            if (imageUrl.isEmpty()) {
                trySend(Resource.Error("Image URL is empty"))
                close() // Close the flow since there's nothing more to emit
                return@callbackFlow
            }

            val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

            photoRef.delete()
                .addOnSuccessListener {
                    Timber.tag("Storage").d("Image deleted successfully")
                    trySend(Resource.Success(Unit))
                    close() // Close the flow after sending success
                }
                .addOnFailureListener { e ->
                    Timber.tag("Storage").e("Failed to delete image: ${e.message}")
                    trySend(Resource.Error("Failed to delete image: ${e.message}"))
                    close() // Close the flow after sending error
                }

        } catch (e: Exception) {
            trySend(Resource.Error("Exception: ${e.message}"))
            close()
        }

        awaitClose {
            // No cleanup required here, but required by callbackFlow
        }
    }


    override fun getUserData(onResult: (Users?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid

        val emailPrefix = currentUser.email?.substringBefore("@")?.trim()?.replace("\\s+".toRegex(), "_")
        val modifiedUserId = if (!emailPrefix.isNullOrEmpty()) {
            "$uid-$emailPrefix"
        } else {
            uid // fallback
        }

        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(modifiedUserId)

        // First attempt: try fetching with modifiedUserId
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                if (user != null) {
                    lastFetchedUser = user
                    onResult(user)
                } else {
                    // If not found, fallback: search all users by uid
                    val allUsersRef = FirebaseDatabase.getInstance().getReference("Users")
                    allUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (child in snapshot.children) {
                                val user = child.getValue(Users::class.java)
                                if (user?.userId == uid) {
                                    lastFetchedUser = user
                                    onResult(user)
                                    return
                                }
                            }
                            onResult(null) // Not found
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onResult(null)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })
    }




    override fun forgotPassword(email: String): Flow<Resource<Unit>> = callbackFlow {
        trySend(Resource.Loading)

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.message ?: "Failed to send password reset email"))
            }

        awaitClose()
    }

    override fun getCurrentUserImgUrl(): String {
        return lastFetchedUser?.imgUrl ?: ""
    }



}