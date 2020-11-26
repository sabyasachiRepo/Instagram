package com.sabya.instagram.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sabya.instagram.R
import com.sabya.instagram.utils.FirebaseHelper
import kotlinx.android.synthetic.main.activity_profile_settings.*

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        mFirebase = FirebaseHelper(this)
        sign_out_text.setOnClickListener {
            mFirebase.auth.signOut()
        }
        back_image.setOnClickListener {
            finish()
        }
    }
}
