package com.song.nafis.nf.TuneLyf.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.UI.AuthViewModel
import com.song.nafis.nf.TuneLyf.databinding.ActivityRegisterBinding
import com.song.nafis.nf.TuneLyf.resource.Loading
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModel.googleSignIn(credential)

        } catch (e: Exception) {
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.register) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCreateAccount.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etRePassword.text.toString().trim()
            val phone = binding.etNumber.text.toString().trim()

            // Empty check
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Email format check
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password length check
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password match check
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Optional: Phone number format (at least 10 digits)
            if (phone.isNotEmpty() && phone.length < 10) {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If all is valid, proceed
            viewModel.register(email, password)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        Loading.show(this@RegisterActivity)
                    }

                    is Resource.Success -> {
                        Loading.hide()
                        Toast.makeText(this@RegisterActivity, "Authentication Successful", Toast.LENGTH_SHORT).show()

                        val currentUser = FirebaseAuth.getInstance().currentUser

                        val name = currentUser?.displayName ?: binding.etName.text.toString().trim()
                        val phone = currentUser?.phoneNumber ?: binding.etNumber.text.toString().trim()
                        val email = currentUser?.email ?: binding.etEmail.text.toString().trim()

                        Timber.tag("AuthViewModel").d("isNewUser: ${viewModel.isNewUser}")

                        if (viewModel.isNewUser) {
                            viewModel.saveUserData(name = name, phoneNumber = phone, email = email, password = "")
                        }

                        val intent = Intent(this@RegisterActivity, DashBoard::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    is Resource.Error -> {
                        Loading.hide()
                        Toast.makeText(this@RegisterActivity, state.message ?: "Something went wrong", Toast.LENGTH_LONG).show()
                    }

                    else -> Loading.hide()
                }
            }
        }


        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

             val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Get this from google-services.json
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            binding.btnGoogle.setOnClickListener {
                    signInLauncher.launch(googleSignInClient.signInIntent)
            }

    }


    override fun onBackPressed() {
        Loading.hide() //
        super.onBackPressed()
    }

}
