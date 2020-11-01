package com.sabya.instagram.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sabya.instagram.R
import com.sabya.instagram.models.User
import com.sabya.instagram.views.PasswordDialog
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private val TAKE_PICTURE_REQUEST_CODE = 1
    private val TAG = "EditProfileActivity"
    var mUser: User? = null
    lateinit var mPendingUser: User
    lateinit var mAuth: FirebaseAuth
    lateinit var mDatabase: DatabaseReference
    lateinit var mStorage: StorageReference
    private val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    private lateinit var mImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")
        close_image.setOnClickListener {
            finish()
        }

        save_image.setOnClickListener { updateProfile() }
        change_photo_text.setOnClickListener { takeCameraPicture() }

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference
        mDatabase.child("users").child(user!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)
                name_input.setText(mUser?.name, TextView.BufferType.EDITABLE)
                username_input.setText(mUser?.username, TextView.BufferType.EDITABLE)
                website_input.setText(mUser?.website, TextView.BufferType.EDITABLE)
                bio_input.setText(mUser?.bio, TextView.BufferType.EDITABLE)
                email_input.setText(mUser?.email, TextView.BufferType.EDITABLE)
                phone_input.setText(mUser?.phone?.toString(), TextView.BufferType.EDITABLE)
                profile_image.loadUserPhoto(mUser?.photo)
            })
    }

    private fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val imageFile = createImageFile()
            mImageUri = FileProvider.getUriForFile(
                this,
                "com.sabya.instagram.fileprovider",
                imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE)
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${simpleDateFormat.format(Date())}_",
            ".jpg",
            storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uid = mAuth.currentUser!!.uid
            val pathReference = mStorage.child("users/$uid/photo")
            val uploadTask = pathReference.putFile(mImageUri)
            val urlTask = uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    pathReference.downloadUrl.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val photoUrl = task.result.toString()
                            mDatabase.child("users/$uid/photo").setValue(photoUrl)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        mUser = mUser?.copy(photo = photoUrl)
                                        profile_image.loadUserPhoto(mUser?.photo)
                                    } else {
                                        showToast(it.exception!!.message!!)
                                    }
                                }
                        } else {
                            showToast(task.exception!!.message!!)
                        }
                    }

                }
            }
        }
    }

    private fun updateProfile() {
        val phoneString = phone_input.text.toString()
        mPendingUser = readInputs(phoneString)

        val error = validate(mPendingUser)
        if (error == null) {
            if (mPendingUser.email == mUser?.email) {
                updateUser(mPendingUser)
            } else {
                PasswordDialog().show(supportFragmentManager, "password_dialog")
            }
        }


    }

    private fun readInputs(phoneString: String): User {
        return User(
            name = name_input.text.toString(),
            username = username_input.text.toString(),
            email = email_input.text.toString(),
            website = website_input.text.toStringOrNull(),
            bio = bio_input.text.toStringOrNull(),
            phone = phone_input.text.toString().toLongOrNull()
        )
    }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUser!!.email, password)
            mAuth.currentUser!!.reauthenticate(credential) {
                mAuth.currentUser!!.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else {
            showToast("You should enter your password")
        }
    }


    private fun updateUser(user: User) {
        val updateMap = mutableMapOf<String, Any?>()
        if (user.name != mUser?.name) updateMap["name"] = user.name
        if (user.username != mUser?.username) updateMap["username"] = user.username
        if (user.website != mUser?.website) updateMap["website"] = user.website
        if (user.bio != mUser?.bio) updateMap["bio"] = user.bio
        if (user.email != mUser?.email) updateMap["email"] = user.email
        if (user.phone != mUser?.phone) updateMap["phone"] = user.phone


        mDatabase.updateUser(mAuth.currentUser!!.uid, updateMap)
        {
            showToast("Profile saved")
            finish()
        }

    }

    private fun DatabaseReference.updateUser(
        uid: String,
        updateMap: Map<String, Any?>,
        onSuccess: () -> Unit
    ) {
        child("users").child(uid).updateChildren(updateMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
    }

    private fun validate(user: User): String? =
        when {
            user.name.isEmpty() -> "Please enter name"
            user.username.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }

    private fun FirebaseUser.updateEmail(email: String, onSuccess: () -> Unit) {
        updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun FirebaseUser.reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

}

