package com.sabya.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sabya.instagram.R
import com.sabya.instagram.utils.CameraHelper
import com.sabya.instagram.utils.GlideApp
import kotlinx.android.synthetic.main.activity_share.*

class ShareActivity : BaseActivity(2) {

    private val TAG = "ShareActivity"

    private lateinit var mCameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        Log.d(TAG, "onCreate")

        mCameraHelper = CameraHelper(this)
        mCameraHelper.takeCameraPicture()

        back_image.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mCameraHelper.REQUEST_CODE && resultCode == RESULT_OK) {
            GlideApp.with(this).load(mCameraHelper.imageUri).centerCrop().into(post_image)
        }
    }
}