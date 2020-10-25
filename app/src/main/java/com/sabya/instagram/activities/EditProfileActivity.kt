package com.sabya.instagram.activities

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sabya.instagram.R
import com.sabya.instagram.models.User
import com.sabya.instagram.views.PasswordDialog
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {

    private val TAG = "EditProfileActivity"
    var mUser: User? = null
    lateinit var mPendingUser: User
    lateinit var mAuth: FirebaseAuth
    lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")
        close_image.setOnClickListener {
            finish()
        }

        save_image.setOnClickListener { updateProfile() }
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child(user!!.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)
                name_input.setText(mUser?.name, TextView.BufferType.EDITABLE)
                username_input.setText(mUser?.username, TextView.BufferType.EDITABLE)
                website_input.setText(mUser?.website, TextView.BufferType.EDITABLE)
                bio_input.setText(mUser?.bio, TextView.BufferType.EDITABLE)
                email_input.setText(mUser?.email, TextView.BufferType.EDITABLE)
                phone_input.setText(mUser?.phone.toString(), TextView.BufferType.EDITABLE)
            })
    }

    private fun updateProfile() {
        mPendingUser = User(
            name = name_input.text.toString(),
            username = username_input.text.toString(),
            website = website_input.text.toString(),
            bio = bio_input.text.toString(),
            email = email_input.text.toString(),
            phone = phone_input.text.toString().toLong()
        )

        val error = validate(mPendingUser)
        if (error == null) {
            if (mPendingUser.email == mUser?.email) {
                updateUser(mPendingUser)
            } else {
                PasswordDialog().show(supportFragmentManager, "password_dialog")
            }
        }


    }

    override fun onPasswordConfirm(password: String) {
        val credential = EmailAuthProvider.getCredential(mUser!!.email, password)
        mAuth.currentUser!!.reauthenticate(credential).addOnCompleteListener { reautenticate ->
            if (reautenticate.isSuccessful) {
                mAuth.currentUser!!.updateEmail(mPendingUser.email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        updateUser(mPendingUser)
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }

            } else {
                showToast(reautenticate.exception!!.message!!)

            }
        }
    }

    private fun updateUser(user: User) {
        val updateMap = mutableMapOf<String, Any>()
        if (user.name != mUser?.name) updateMap["name"] = user.name
        if (user.username != mUser?.username) updateMap["username"] = user.username
        if (user.website != mUser?.website) updateMap["website"] = user.website
        if (user.bio != mUser?.bio) updateMap["bio"] = user.bio
        if (user.email != mUser?.email) updateMap["email"] = user.email
        if (user.phone != mUser?.phone) updateMap["phone"] = user.phone


        mDatabase.child("users").child(mAuth.currentUser!!.uid).updateChildren(updateMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    showToast("Profile saved")
                    finish()
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


}

