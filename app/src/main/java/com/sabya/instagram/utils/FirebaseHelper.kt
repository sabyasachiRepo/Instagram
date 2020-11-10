package com.sabya.instagram.utils

import android.app.Activity
import android.net.Uri
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sabya.instagram.activities.showToast

class FirebaseHelper(val activity: Activity) {

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mStorage: StorageReference = FirebaseStorage.getInstance().reference

    fun updateUserPhoto(
        photoUrl: String,
        onSuccess: () -> Unit
    ) {
        mDatabase.child("users/${mAuth.currentUser!!.uid}/photo").setValue(photoUrl)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()

                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }


    fun updateUser(
        updateMap: Map<String, Any?>,
        onSuccess: () -> Unit
    ) {
        mDatabase.child("users").child(mAuth.currentUser!!.uid).updateChildren(updateMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    activity.showToast(it.exception!!.message!!)
                }
            }
    }

    fun uploadUserPhoto(
        photo: Uri,
        onSuccess: (String) -> Unit
    ) {
        val pathReference = mStorage.child("users/${mAuth.currentUser!!.uid}/photo")
        val uploadTask = mStorage.child("users/${mAuth.currentUser!!.uid}/photo").putFile(photo)
        val urlTask = uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                pathReference.downloadUrl.addOnCompleteListener { taskUri ->
                    if (taskUri.isSuccessful) {
                        onSuccess(taskUri.result.toString())
                    } else {
                        activity.showToast(taskUri.exception!!.message!!)
                    }

                }

            }
        }
    }

    fun updateEmail(email: String, onSuccess: () -> Unit) {
        mAuth.currentUser!!.updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        mAuth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun currentUserReference(): DatabaseReference =
        mDatabase.child("users").child(mAuth.currentUser!!.uid)
}