package com.sabya.instagram

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class LoginActivity : AppCompatActivity(), KeyboardVisibilityEventListener, TextWatcher {
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate")
        KeyboardVisibilityEvent.registerEventListener(this, this)
        login_btn.isEnabled = false
        email_input.addTextChangedListener(this)
        password_input.addTextChangedListener(this)
    }

    override fun onVisibilityChanged(isKeyBoardOpen: Boolean) {
        if (isKeyBoardOpen) {
            scroll_view.scrollTo(0, scroll_view.bottom)
            create_account_text.visibility = View.GONE
        } else {
            scroll_view.scrollTo(0, scroll_view.top)
            create_account_text.visibility = View.VISIBLE

        }
    }


    override fun afterTextChanged(s: Editable?) {
        login_btn.isEnabled =
            email_input.text.toString().isNotEmpty() && password_input.text.toString().isNotEmpty()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }


}