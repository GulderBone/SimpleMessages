package com.gulderbone.simple_messages.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.gulderbone.simple_messages.base.BaseActivity
import com.gulderbone.simple_messages.RequestCode
import com.gulderbone.simple_messages.databinding.ActivityRegisterBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.messages.LatestMessagesActivity
import com.gulderbone.simple_messages.models.User
import java.util.*

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButtonRegister.setOnClickListener {
            performRegister()
        }

        binding.alreadyHaveAccountTextviewRegister.setOnClickListener {
            Log.d(TAG, "Try to show login activity")

            // launch the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.selectphotoButtonRegister.setOnClickListener {
            Log.d(TAG, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, RequestCode.PHOTO_PICKER_REGISTRATION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RequestCode.PHOTO_PICKER_REGISTRATION -> {
                if (resultCode == Activity.RESULT_OK && data != null) {

                    selectedPhotoUri = data.data ?: Uri.EMPTY

                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

                    binding.selectphotoImageviewRegister.setImageBitmap(bitmap)

                    binding.selectphotoButtonRegister.alpha = 0f
                }
            }
        }
    }

    private fun performRegister() {
        val email = binding.emailEdittextRegister.editText?.text.toString()
        val password = binding.passwordEdittextRegister.editText?.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Email: $email")
        Log.d(TAG, "Password: $password")

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d(TAG, "Successfully created user with uid: ${it.result?.user?.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to create user: ${it.message}")
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val reference = FirebaseStorage.getInstance().getReference("/images/$filename")

        reference.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                reference.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "uploadImageToFirebaseStorage: Failed")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance("https://messenger-72529-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users/$uid")

        val user = User(uid, binding.usernameEdittextRegister.editText?.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Finally we saved the user to Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.e(TAG, "saveUserToFirebaseDatabase: fail", it)
            }
    }
}