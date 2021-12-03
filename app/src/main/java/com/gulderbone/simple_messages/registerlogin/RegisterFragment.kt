package com.gulderbone.simple_messages.registerlogin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.base.BaseFragment
import com.gulderbone.simple_messages.main.MainViewModel
import com.gulderbone.simple_messages.databinding.FragmentRegisterBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.extensions.visibleOrGone
import com.gulderbone.simple_messages.models.User
import com.gulderbone.simple_messages.utils.CountingIdlingResourceSingleton
import com.gulderbone.simple_messages.utils.RequestCode
import com.vmadalin.easypermissions.EasyPermissions
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class RegisterFragment : BaseFragment<FragmentRegisterBinding>(), EasyPermissions.PermissionCallbacks {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegisterBinding =
        FragmentRegisterBinding::inflate

    private lateinit var navController: NavController

    private val mainViewModel by lazy { ViewModelProvider(requireActivity()).get(MainViewModel::class.java) }

    private var selectedPhotoUri: Uri? = null
    private var selectedPhotoFile: File? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        with(binding) {
            selectphotoImageviewRegister.isVisible = false

            alreadyHaveAccountTextviewRegister.setOnClickListener {
                navController.navigate(R.id.action_registerFragment_to_loginFragment)
            }

            selectphotoButtonRegister.setOnClickListener {
                chooseProfilePicture()
            }

            registerButtonRegister.setOnClickListener {
                performRegister()
            }

            mainViewModel.loaderVisibility().observe(viewLifecycleOwner, {
                loader.loader.visibleOrGone(it)
            })
        }
    }

    private fun chooseProfilePicture() {
        if (EasyPermissions.hasPermissions(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            openImagePicker()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Permission to read files is required to choose profile picture",
                RequestCode.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, RequestCode.PHOTO_PICKER_REGISTRATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RequestCode.PHOTO_PICKER_REGISTRATION -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    selectedPhotoUri = data.data ?: Uri.EMPTY

                    userSelectedProfilePicture()
                }
            }
        }
    }

    private fun userSelectedProfilePicture() {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPhotoUri)
        } else {
            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedPhotoUri!!)
            ImageDecoder.decodeBitmap(source)
        }

        selectedPhotoFile = File.createTempFile("registrationImage", ".png")
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, selectedPhotoFile?.outputStream())

        binding.selectphotoImageviewRegister.setImageBitmap(bitmap)
        binding.selectphotoImageviewRegister.isVisible = true

        binding.selectphotoButtonRegister.alpha = 0f
    }

    private fun performRegister() {
        val email = binding.emailEdittextRegister.editText?.text.toString()
        val password = binding.passwordEdittextRegister.editText?.text.toString()

        if (!validateRegistrationInputs(email, password)) return

        CountingIdlingResourceSingleton.increment() // TODO Replace with loader

        mainViewModel.setLoaderVisibility(true)

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d(TAG, "Successfully created user with uid: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to create user: ${it.message}")
            }
    }

    private fun validateRegistrationInputs(email: String, password: String): Boolean {
        if (!binding.selectphotoImageviewRegister.isVisible) {
            Toast.makeText(requireActivity(), "Please choose a profile picture", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireActivity(), "Please enter login and / or password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadImageToFirebaseStorage() {
        CoroutineScope(Main).launch {
            val compressedPhotoUri: Uri = Compressor.compress(requireActivity(), selectedPhotoFile!!) {
                if (Build.VERSION.SDK_INT < 30) {
                    default(format = Bitmap.CompressFormat.WEBP)
                } else {
                    default(format = Bitmap.CompressFormat.WEBP_LOSSLESS)
                }
            }.toUri()

            val filename = UUID.randomUUID().toString()
            val reference = FirebaseStorage.getInstance().getReference("/images/$filename")

            reference.putFile(compressedPhotoUri)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                    reference.downloadUrl.addOnSuccessListener { fileUri ->
                        Log.d(TAG, "File Location: $fileUri")

                        saveUserToFirebaseDatabase(fileUri.toString())
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Uploading profile picture failed: ${it.localizedMessage}")
                }
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""

        val username = binding.usernameEdittextRegister.editText?.text.toString()
        val user = User(uid, username, profileImageUrl)

        val registerReference = Firebase.firestore.document("/users/$uid")
        registerReference.set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User saved to Firestore")

                navController.navigate(R.id.latestMessagesFragment)
            }
            .addOnFailureListener {
                Log.e(TAG, "Saving user to Firebase Database failed: ${it.localizedMessage}")
            }
    }

    // EasyPermissions section
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        openImagePicker()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            requireActivity(),
            "Permission to read files is required to choose profile picture",
            Toast.LENGTH_LONG
        ).show()
    }
    // EasyPermissions section end
}