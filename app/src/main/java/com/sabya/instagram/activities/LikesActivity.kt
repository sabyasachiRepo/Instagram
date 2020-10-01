package com.sabya.instagram.activities

import android.os.Bundle
import android.util.Log
import com.sabya.instagram.R

class LikesActivity : BaseActivity(3) {

    private val TAG = "LikesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setUpBottomNavigation()
        Log.d(TAG, "onCreate")

    }
}