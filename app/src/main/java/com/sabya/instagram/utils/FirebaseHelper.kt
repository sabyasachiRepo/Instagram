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

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var database: DatabaseReference = FirebaseDatabase.getInstance().reference
    var storage: StorageReference = FirebaseStorage.getInstance().reference

    fun updateUserPhoto(
        photoUrl: String,
        onSuccess: () -> Unit
    ) {
        database.child("users/${currentUid()!!}/photo").setValue(photoUrl)
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
        database.child("users").child(currentUid()!!).updateChildren(updateMap)
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
        val pathReference = storage.child("users/${currentUid()!!}/photo")
        pathReference.putFile(photo).addOnCompleteListener { task ->
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
        auth.currentUser!!.updateEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        auth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            } else {
                activity.showToast(it.exception!!.message!!)
            }
        }
    }

    fun currentUserReference(): DatabaseReference =
        database.child("users").child(currentUid()!!)

    fun currentUid(): String? = auth.currentUser?.uid
}