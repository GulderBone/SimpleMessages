package com.gulderbone.simple_messages.registerlogin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.gulderbone.simple_messages.base.BaseActivity
import com.gulderbone.simple_messages.databinding.ActivityRegisterBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.extensions.getFilePathFromContentUri
import com.gulderbone.simple_messages.presentation.latestmessages.LatestMessagesActivity
import com.gulderbone.simple_messages.models.User
import com.gulderbone.simple_messages.utils.Constant
import com.gulderbone.simple_messages.utils.RequestCode
import com.vmadalin.easypermissions.EasyPermissions
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class RegisterActivity : BaseActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityRegisterBinding

    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectphotoImageviewRegister.isVisible = false

        binding.alreadyHaveAccountTextviewRegister.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.selectphotoButtonRegister.setOnClickListener {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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

        binding.registerButtonRegister.setOnClickListener {
            performRegister()
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
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, selectedPhotoUri!!)
            ImageDecoder.decodeBitmap(source)
        }

        binding.selectphotoImageviewRegister.setImageBitmap(bitmap)
        binding.selectphotoImageviewRegister.isVisible = true

        binding.selectphotoButtonRegister.alpha = 0f
    }

    private fun performRegister() {
        val email = binding.emailEdittextRegister.editText?.text.toString()
        val password = binding.passwordEdittextRegister.editText?.text.toString()

        if (!binding.selectphotoImageviewRegister.isVisible) {
            Toast.makeText(this, "Please choose a profile picture", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }

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

    private fun uploadImageToFirebaseStorage() {
        val photoUri = selectedPhotoUri ?: return
        val filePath = photoUri.getFilePathFromContentUri(contentResolver) ?: return

        val photoFile = File(filePath)

        CoroutineScope(Main).launch {
            val compressedPhotoUri: Uri = Compressor.compress(this@RegisterActivity, photoFile).toUri()

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
        val ref = FirebaseDatabase.getInstance(Constant.FIREBASE_DATABASE_URL).getReference("users/$uid")

        val username = binding.usernameEdittextRegister.editText?.text.toString()
        val user = User(uid, username, profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "User saved to Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
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
        Toast.makeText(this, "Permission to read files is required to choose profile picture", Toast.LENGTH_LONG).show()
    }
    // EasyPermissions section end
}