package com.song.nafis.nf.TuneLyf.Activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.UI.AuthViewModel
import com.song.nafis.nf.TuneLyf.databinding.ActivityOtpBinding
import com.song.nafis.nf.TuneLyf.resource.Loading
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private val viewModel: AuthViewModel by viewModels()
    private var verificationId: String? = null
    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.otpGroup.visibility = View.GONE
        binding.btnVerifyOtp.text = "Send OTP"
        binding.tvResendOtp.visibility = View.GONE

        // Observe auth state once
// Observe OTP sending (verificationId)
        lifecycleScope.launchWhenStarted {
            viewModel.sendOtpState.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Loading.hide()
                        verificationId = resource.data
                        Toast.makeText(this@OtpActivity, "OTP sent", Toast.LENGTH_SHORT).show()
                        showOtpInputUI()
                        startResendTimer()
                    }
                    is Resource.Error -> {
                        Loading.hide()
                        Toast.makeText(this@OtpActivity, resource.message ?: "Failed to send OTP", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        Loading.show(this@OtpActivity)
                    }
                    else -> Unit
                }
            }
        }

// Observe OTP verification (true/false)
        lifecycleScope.launchWhenStarted {
            viewModel.otpState.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        Loading.hide()
                        if (resource.data) {
                            Toast.makeText(this@OtpActivity, "OTP Verified", Toast.LENGTH_SHORT).show()
                            // ✅ OTP Verified
                            viewModel.fetchUserData { user ->
                                if (user == null) {
                                    // 🚨 New user: Show popup to collect name and optional info
                                    showDetailsPopup()
                                } else {
                                    // ✅ Existing user: Go to dashboard
                                    sendToMain()
                                }
                            }
                        } else {
                            Toast.makeText(this@OtpActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is Resource.Error -> {
                        Loading.hide()
                        Toast.makeText(this@OtpActivity, resource.message ?: "Verification failed", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        // Show loading spinner for OTP verification
                        Loading.show(this@OtpActivity)
                    }
                    else -> Unit
                }
            }
        }

        binding.btnVerifyOtp.setOnClickListener {
            var phone = binding.etNumber.text.toString().trim()
            if (!phone.startsWith("+")) {
                phone = "+91$phone"
            }
            if (binding.otpGroup.visibility == View.GONE) {
                if (phone.isNotEmpty()) {
                    viewModel.sendOtp(phone,this@OtpActivity) // just call it, no collect here
                } else {
                    Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
                }
            } else {
                val otp = binding.etOtp.text.toString().trim()
                if (otp.isNotEmpty() && verificationId != null) {
                    viewModel.verifyOtp(verificationId!!, otp) // call verifyOtp here
                } else {
                    Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvResendOtp.setOnClickListener {
            var phone = binding.etNumber.text.toString().trim()
            if (!phone.startsWith("+")) {
                phone = "+91$phone"
            }
            if (phone.isNotEmpty()) viewModel.sendOtp(phone,this@OtpActivity)
        }
    }



    private fun showOtpInputUI() {
        binding.otpGroup.visibility = View.VISIBLE
        binding.btnVerifyOtp.text = "Verify OTP"
        binding.tvResendOtp.visibility = View.VISIBLE
    }

    private fun startResendTimer() {
        binding.tvResendOtp.isEnabled = false
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvResendOtp.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                binding.tvResendOtp.text = "Didn’t receive code? Resend"
                binding.tvResendOtp.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }



    private fun showDetailsPopup() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_data_using_phone_otp)
        dialog.setCancelable(false)

        // Optional: Set custom background and animations
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Get references to views
        val nameInput = dialog.findViewById<EditText>(R.id.Update_name)
        val emailInput = dialog.findViewById<EditText>(R.id.showEmail)
        val saveButton = dialog.findViewById<AppCompatButton>(R.id.final_profile_update_button)
        val cancelButton = dialog.findViewById<AppCompatButton>(R.id.final_profile_cancel_button)

        // Button click listeners
        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()

            when {
                name.isEmpty() -> {
                    nameInput.error = "Name is required"
                    nameInput.requestFocus()
                }
                email.isEmpty() -> {
                    emailInput.error = "Email is required"
                    emailInput.requestFocus()
                }
                else -> {
                    viewModel.saveUserData(
                        name = name,
                        email = email,
                        phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
                    )
                    dialog.dismiss()
                    sendToMain()
                }
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun sendToMain() {
        val intent = Intent(this, DashBoard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}
