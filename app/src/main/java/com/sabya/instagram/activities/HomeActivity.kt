package com.sabya.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sabya.instagram.R
import com.sabya.instagram.utils.FirebaseHelper
import com.sabya.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity(0) {

    private val TAG = "HomeActivity"
    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setUpBottomNavigation()
        Log.d(TAG, "onCreate")

        mFirebase = FirebaseHelper(this)

        sign_out_text.setOnClickListener {
            mFirebase.auth.signOut()
        }
        mFirebase.auth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        mFirebase.database.child("feed-post").child(mFirebase.auth.currentUser!!.uid)
            .addValueEventListener(ValueEventListenerAdapter {
                val posts = it.children.map { it.getValue(FeedPost::class.java) }
                Log.d(TAG, "feedPosts: ${posts.first()!!.timeStampDate()})")
            })

    }

    override fun onStart() {
        super.onStart()
        if (mFirebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

}