package com.song.nafis.nf.TuneLyf.Activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.UI.AuthViewModel
import com.song.nafis.nf.TuneLyf.databinding.ActivityLoginBinding
import com.song.nafis.nf.TuneLyf.databinding.ForgotPassLayoutBinding
import com.song.nafis.nf.TuneLyf.resource.AuthSuccess
import com.song.nafis.nf.TuneLyf.resource.Loading
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private var forgotDialog: Dialog? = null


    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            viewModel.googleSignIn(credential)  // Use this in your ViewModel
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Login Button Click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Sign-In Placeholder
        binding.btnGoogle.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // From google-services.json
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(this, options)
            googleSignInLauncher.launch(client.signInIntent)
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Navigate to Register
        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

//        //login using number
//        binding.btnCreateUsingPhone.setOnClickListener {
//            startActivity(Intent(this, OtpActivity::class.java))
//        }

        // Auth State Observer
        lifecycleScope.launchWhenStarted {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        Loading.show(this@LoginActivity)
                    }

                    is Resource.Success -> {
                        Loading.hide()
                        val currentUser = FirebaseAuth.getInstance().currentUser

                        when (state.data) {
                            AuthSuccess.Login -> {
                                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                                // Save user data if it's a new user
                                if (viewModel.isNewUser && currentUser != null) {
                                    val name = currentUser.displayName ?: ""
                                    val phone = currentUser.phoneNumber ?: ""
                                    val email = currentUser.email ?: ""

                                    viewModel.saveUserData(
                                        name = name,
                                        phoneNumber = phone,
                                        email = email,
                                        password = "" // Never store plain password
                                    )
                                }

                                startActivity(Intent(this@LoginActivity, DashBoard::class.java))
                                finish()
                            }

                            AuthSuccess.Register -> {
                                Toast.makeText(this@LoginActivity, "Registration successful", Toast.LENGTH_SHORT).show()

                                // Save user data on successful registration
                                if (currentUser != null) {
                                    val name = currentUser.displayName ?: ""
                                    val phone = currentUser.phoneNumber ?: ""
                                    val email = currentUser.email ?: ""

                                    viewModel.saveUserData(
                                        name = name,
                                        phoneNumber = phone,
                                        email = email,
                                        password = "" // Never store plain password
                                    )
                                }
                            }

                            AuthSuccess.ForgotPassword -> {
                                Toast.makeText(this@LoginActivity, "Please check your email for reset link", Toast.LENGTH_SHORT).show()
                            }

                            is AuthSuccess.Other -> {
                                Toast.makeText(this@LoginActivity, state.data.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    is Resource.Error -> {
                        Loading.hide()
                        Toast.makeText(this@LoginActivity, state.message ?: "Unknown error", Toast.LENGTH_LONG).show()
                    }

                    else -> Loading.hide()
                }
            }
        }
    }


    private fun showForgotPasswordDialog() {
        val dialogBinding = ForgotPassLayoutBinding.inflate(LayoutInflater.from(this))
        forgotDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        forgotDialog?.show()

        dialogBinding.forgotbtn.setOnClickListener {
            val email = dialogBinding.fogotemail.editText?.text.toString()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                viewModel.forgotPassword(email)
                dismissAllDialogs()
            } else {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
            }

        }

        dialogBinding.forgotcancel.setOnClickListener {
            dismissAllDialogs()
        }
    }

    private fun dismissAllDialogs() {
        forgotDialog?.dismiss()
    }

}
