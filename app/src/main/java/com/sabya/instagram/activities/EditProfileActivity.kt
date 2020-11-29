package com.sabya.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.sabya.instagram.R
import com.sabya.instagram.models.User
import com.sabya.instagram.utils.CameraHelper
import com.sabya.instagram.utils.FirebaseHelper
import com.sabya.instagram.utils.ValueEventListenerAdapter
import com.sabya.instagram.views.PasswordDialog
import kotlinx.android.synthetic.main.activity_edit_profile.*


class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private val TAG = "EditProfileActivity"
    var mUser: User? = null
    lateinit var mPendingUser: User
    lateinit var mFirebase: FirebaseHelper
    lateinit var mCamera: CameraHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        mCamera = CameraHelper(this)
        Log.d(TAG, "onCreate")
        close_image.setOnClickListener {
            finish()
        }

        save_image.setOnClickListener { updateProfile() }
        change_photo_text.setOnClickListener { mCamera.takeCameraPicture() }

        mFirebase = FirebaseHelper(this)
        mFirebase.currentUserReference()
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.asUser()
                name_input.setText(mUser?.name)
                username_input.setText(mUser?.username)
                website_input.setText(mUser?.website)
                bio_input.setText(mUser?.bio)
                email_input.setText(mUser?.email)
                phone_input.setText(mUser?.phone?.toString())
                profile_image.loadUserPhoto(mUser?.photo)
            })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCamera.REQUEST_CODE && resultCode == RESULT_OK) {
            mFirebase.uploadUserPhoto(mCamera.imageUri!!) { photoUrl ->
                mFirebase.updateUserPhoto(photoUrl) {
                    mUser = mUser?.copy(photo = photoUrl)
                    profile_image.loadUserPhoto(mUser?.photo)
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
            mFirebase.reauthenticate(credential) {
                mFirebase.updateEmail(mPendingUser.email) {
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


        mFirebase.updateUser(updateMap)
        {
            showToast("Profile saved")
            finish()
        }

    }


    private fun validate(user: User): String? =
        when {
            user.name.isEmpty() -> "Please enter name"
            user.username.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }


}

