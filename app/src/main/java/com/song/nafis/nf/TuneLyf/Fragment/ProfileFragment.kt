package com.song.nafis.nf.TuneLyf.Fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.song.nafis.nf.TuneLyf.Activity.About
import com.song.nafis.nf.TuneLyf.Activity.LoginActivity
import com.song.nafis.nf.TuneLyf.Activity.AppSetting
import com.song.nafis.nf.TuneLyf.Activity.Help
import com.song.nafis.nf.TuneLyf.Activity.PrivacyPolicy
import com.song.nafis.nf.TuneLyf.Activity.RecentPlayList
import com.song.nafis.nf.TuneLyf.FavoriteMusic
import com.song.nafis.nf.TuneLyf.MainActivity
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline
import com.song.nafis.nf.TuneLyf.UI.AuthViewModel
import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
import com.song.nafis.nf.TuneLyf.databinding.FragmentProfileBinding
import com.song.nafis.nf.TuneLyf.resource.Loading
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: AuthViewModel by viewModels()
    private val musicViewModel: MusicViewModel by viewModels()
    private lateinit var imgDialog: ImageView
    var selectedImageUri: Uri? = null
    var uploadedImageUrl: String? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    viewModel.uploadProfilePhoto(uri)
                    if (::imgDialog.isInitialized) {
                        Glide.with(requireContext()).load(uri).into(imgDialog)
                    }
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.shimmerProfile.shimmerLayout.visibility = View.VISIBLE
        binding.circleProfImageView.visibility = View.GONE
        binding.shimmerTextProfile.shimmerTextLayout.visibility= View.VISIBLE
        binding.proDetail.visibility= View.GONE
        setProfile()
        buttonClick()
        return binding.root
    }

    private fun buttonClick() {
        binding.btnEditProfileImage.setOnClickListener {
            updateProfileDialogBox()
        }

        binding.btnLogout.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
                .setTitle("LogOut")
                .setMessage("Are you Sure to LogOut")
                .setPositiveButton("Yes") { _, _ ->
                    // Stop the music service
                    val stopIntent = Intent(requireContext(), MusicServiceOnline::class.java)
                    requireContext().stopService(stopIntent)

// Stop playback in repository
                    musicViewModel.cancelStopTimer()
                    musicViewModel.playerRepository.stopCurrentSong()
                    MusicServiceOnline.isServiceStopped = true
                    musicViewModel.isPlaying.postValue(false)

// Now logout and redirect
                    viewModel.logout()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

            val alertDialog = builder.create()
            alertDialog.show()
            val color = ContextCompat.getColor(requireContext(), R.color.icon_color)
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color)
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color)
        }

        binding.mymusic.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        binding.setting.setOnClickListener {
               startActivity(Intent(requireContext(), AppSetting::class.java))
        }

        binding.recentlyPlayed.setOnClickListener {
               startActivity(Intent(requireContext(), RecentPlayList::class.java))
        }

        binding.aboutApp.setOnClickListener {
               startActivity(Intent(requireContext(), About::class.java))
        }

        binding.privacyPolicy.setOnClickListener {
               startActivity(Intent(requireContext(), PrivacyPolicy::class.java))
        }
        binding.helpSupport.setOnClickListener {
               startActivity(Intent(requireContext(), Help::class.java))
        }

        binding.favoritesSongs.setOnClickListener {
               startActivity(Intent(requireContext(), FavoriteMusic::class.java))
        }
    }

    private fun setProfile() {
        viewModel.fetchUserData { user ->
            if (user != null) {
                binding.profName.text = user.name.ifEmpty { "" }
                binding.profEmail.text=user.email.ifEmpty { "" }

                if (user.imgUrl.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(user.imgUrl)
                        .placeholder(R.drawable.profileicon)
                        .error(R.drawable.profileicon)
                        .into(binding.circleProfImageView)
                    // Once loaded

                    binding.shimmerProfile.shimmerLayout.stopShimmer()
                    binding.shimmerProfile.shimmerLayout.visibility = View.GONE
                    binding.circleProfImageView.visibility = View.VISIBLE
                    binding.shimmerTextProfile.shimmerTextLayout.stopShimmer()
                    binding.shimmerTextProfile.shimmerTextLayout.visibility= View.GONE
                    binding.proDetail.visibility= View.VISIBLE
                } else {
                    binding.circleProfImageView.setImageResource(R.drawable.profileicon)
                    binding.shimmerProfile.shimmerLayout.visibility = View.GONE
                    binding.circleProfImageView.visibility = View.VISIBLE
                    binding.shimmerTextProfile.shimmerTextLayout.visibility= View.GONE
                    binding.proDetail.visibility= View.VISIBLE
                }
            } else {
                binding.profName.text = ""
                binding.profEmail.text= ""
                binding.circleProfImageView.setImageResource(R.drawable.profileicon)
                binding.shimmerProfile.shimmerLayout.visibility = View.GONE
                binding.circleProfImageView.visibility = View.VISIBLE
                binding.shimmerTextProfile.shimmerTextLayout.visibility= View.GONE
                binding.proDetail.visibility= View.VISIBLE
            }
        }
    }


    private fun updateProfileDialogBox() {
        try {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.edit_all_detail)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.show()


            // Initialize views
            imgDialog = dialog.findViewById(R.id.dialogcircleProfImageView)
            val editButton = dialog.findViewById<ImageView>(R.id.dialogbtnEditProfileImage)
            val updateName = dialog.findViewById<EditText>(R.id.Update_name)
            val updateNumber = dialog.findViewById<EditText>(R.id.updateNumber)
            val updateEmail = dialog.findViewById<EditText>(R.id.dialogshowEmail)
            val updateButton = dialog.findViewById<Button>(R.id.dialog_profile_update_button)
            val cancelButton = dialog.findViewById<Button>(R.id.dialog_profile_cancel_button)
            val shimmerProfile = dialog.findViewById<com.facebook.shimmer.ShimmerFrameLayout>(R.id.dialog_shimmer_profile)


            shimmerProfile.visibility= View.VISIBLE
            imgDialog.visibility= View.GONE


            viewModel.fetchUserData { user ->
                if (user != null) {
                    updateName.setText(user.name.ifEmpty { "" })
                    updateEmail.setText(user.email.ifEmpty { "" })
                    updateNumber.setText(user.phone.ifEmpty { "" })

                    if (user.imgUrl.isNotEmpty()) {
                        Glide.with(requireContext())
                            .load(user.imgUrl)
                            .placeholder(R.drawable.profileicon)
                            .error(R.drawable.profileicon)
                            .into(imgDialog)

                    } else {
                        imgDialog.setImageResource(R.drawable.profileicon)
                    }
                } else {
                    updateName.setText("")
                    updateEmail.setText("")
                    updateNumber.setText( "")
                    imgDialog.setImageResource(R.drawable.profileicon)
                }
                shimmerProfile.stopShimmer()
                shimmerProfile.visibility = View.GONE
                imgDialog.visibility = View.VISIBLE
            }






            // Observe upload result and store image URL
            lifecycleScope.launchWhenStarted {
                viewModel.authState.collect { result ->
                    when (result) {
                        is Resource.Loading -> Loading.show(requireContext())
                        is Resource.Success -> {
                            Loading.hide()
                            uploadedImageUrl = result.data.toString()
                            Glide.with(requireContext())
                                .load(uploadedImageUrl)
                                .into(imgDialog)
                            shimmerProfile.stopShimmer()
                            shimmerProfile.visibility = View.GONE
                            imgDialog.visibility= View.VISIBLE
                        }
                        is Resource.Error -> {
                            Loading.hide()
                            Toast.makeText(requireContext(), result.message ?: "Upload failed", Toast.LENGTH_SHORT).show()
                        }
                        else -> Loading.hide()
                    }
                }
            }

            editButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                imagePickerLauncher.launch(intent)
            }



            updateButton.setOnClickListener {
                val name = updateName.text.toString()
                val number = updateNumber.text.toString()
                val email = updateEmail.text.toString()

                if (name.isEmpty() || number.isEmpty() || email.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val oldImageUrl = viewModel.getCurrentUserImageUrl()
                val finalImgUrl = uploadedImageUrl ?: oldImageUrl


                lifecycleScope.launchWhenStarted {
                    viewModel.updateUserDetails(name, number, email, "", finalImgUrl).collect { updateResult ->
                        when (updateResult) {
                            is Resource.Loading -> Loading.show(requireContext())
                            is Resource.Success -> {
                                Loading.hide()

                                Timber.tag("ImgUrl").d(finalImgUrl)
                                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                setProfile()

                                if (uploadedImageUrl != null && uploadedImageUrl != oldImageUrl) {
                                    viewModel.deleteImageFromStorage(oldImageUrl)
                                }
                                shimmerProfile.stopShimmer()
                                shimmerProfile.visibility = View.GONE
                                imgDialog.visibility= View.VISIBLE
                            }
                            is Resource.Error -> {
                                Loading.hide()
                                uploadedImageUrl?.let { viewModel.deleteImageFromStorage(it) }
                                Toast.makeText(requireContext(), updateResult.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
                uploadedImageUrl?.let { viewModel.deleteImageFromStorage(it) }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Unexpected error", Toast.LENGTH_SHORT).show()
            Timber.tag("upload").d(e.message.toString())
        }
    }



    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchItem = menu.findItem(R.id.search_playlist)
        searchItem?.isVisible = false // Hide search in this fragment
    }


}

