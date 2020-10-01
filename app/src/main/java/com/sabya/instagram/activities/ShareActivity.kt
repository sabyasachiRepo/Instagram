package com.sabya.instagram.activities

import android.os.Bundle
import android.util.Log
import com.sabya.instagram.R

class ShareActivity : BaseActivity(2) {

    private val TAG = "ShareActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setUpBottomNavigation()
        Log.d(TAG, "onCreate")

    }
}