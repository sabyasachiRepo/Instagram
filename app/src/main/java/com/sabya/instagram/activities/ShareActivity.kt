package com.sabya.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sabya.instagram.R
import com.sabya.instagram.models.FeedPost
import com.sabya.instagram.models.User
import com.sabya.instagram.utils.CameraHelper
import com.sabya.instagram.utils.FirebaseHelper
import com.sabya.instagram.utils.GlideApp
import com.sabya.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_share.*

class ShareActivity : BaseActivity(2) {

    private val TAG = "ShareActivity"

    private lateinit var mCamera: CameraHelper
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        Log.d(TAG, "onCreate")

        mFirebase = FirebaseHelper(this)

        mCamera = CameraHelper(this)
        mCamera.takeCameraPicture()

        back_image.setOnClickListener { finish() }
        share_text.setOnClickListener { share() }

        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.asUser()!!
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCamera.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                GlideApp.with(this).load(mCamera.imageUri).centerCrop().into(post_image)
            } else {
                finish()
            }
        }
    }

    private fun share() {
        val imageUri = mCamera.imageUri
        if (imageUri != null) {
            val uid = mFirebase.currentUid()!!
            val pathReference = mFirebase.storage.child("users").child(uid).child("images")
                .child(imageUri.lastPathSegment!!)
            pathReference.putFile(imageUri).addOnCompleteListener {
                pathReference.downloadUrl.addOnCompleteListener { taskUri ->
                    if (taskUri.isSuccessful) {
                        val imageDownloadUrl = taskUri.result.toString()
                        mFirebase.database.child("images").child(uid).push()
                            .setValue(imageDownloadUrl).addOnCompleteListener {
                                if (it.isSuccessful) {

                                    mFirebase.database.child("feed-post").child(uid)
                                        .push()
                                        .setValue(mkFeedPost(uid, imageDownloadUrl))
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        ProfileActivity::class.java
                                                    )
                                                )
                                                finish()

                                            }
                                        }
                                } else {
                                    showToast(it.exception!!.message!!)
                                }
                            }

                    } else {
                        showToast(it.exception!!.message!!)
                    }

                }
            }
        }
    }

    private fun mkFeedPost(
        uid: String,
        imageDownloadUrl: String
    ) =
        FeedPost(
            uid = uid,
            userName = mUser.username,
            image = imageDownloadUrl,
            caption = caption_input.text.toString(),
            photo = mUser.photo
        )
}

