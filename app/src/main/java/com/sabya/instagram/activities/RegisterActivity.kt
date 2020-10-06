package com.sabya.instagram.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sabya.instagram.R
import com.sabya.instagram.models.User
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_namepass.*

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener, NamePassFragment.Listener {
    private val TAG = "RegisterActivity"
    private var mEmail: String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.frame_layout, EmailFragment())
                .commit()
        }

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
    }

    override fun onNext(email: String) {
        if (email.isNotEmpty()) {
            mEmail = email
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, NamePassFragment())
                .addToBackStack(null)
                .commit()
        } else {
            showToast("Please enter email")
        }
    }

    override fun onRegister(fullName: String, password: String) {
        if (fullName.isNotEmpty() && password.isNotEmpty()) {
            val email = mEmail
            if (email != null) {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = mkUser(fullName, email)
                        val reference: DatabaseReference? =
                            it.result?.user?.uid?.let { it -> mDatabase.child("users").child(it) }
                        reference?.setValue(user)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                startHomeActivity()
                            } else {
                                unknownRegisterError(task)
                            }
                        }
                    } else {
                        unknownRegisterError(it)
                    }
                }
            } else {
                Log.e(TAG, "onRegister email is null")
                showToast("Please enter email")
                supportFragmentManager.popBackStack()
            }

        } else {
            showToast("Please enter full name and password")
        }
    }

    private fun unknownRegisterError(task: Task<*>) {
        Log.d(TAG, "failed to create user profile", task.exception)
        showToast("Something wrong happened,Please try again later")
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun mkUser(
        fullName: String,
        email: String
    ): User {
        val username = mkUserName(fullName)
        return User(name = fullName, username = username, email = email)
    }

    private fun mkUserName(fullName: String): String = fullName.toLowerCase().replace(" ", ".")
}

class EmailFragment : Fragment() {

    private lateinit var mListener: Listener

    interface Listener {
        fun onNext(email: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        next_btn.setOnClickListener {
            val email = email_input.text.toString()
            mListener.onNext(email)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }

}

class NamePassFragment : Fragment() {

    private lateinit var mListener: Listener

    interface Listener {
        fun onRegister(fullName: String, password: String)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_namepass, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register_btn.setOnClickListener {
            val fullName = full_name_input.text.toString()
            val password = password_input.text.toString()
            mListener.onRegister(fullName, password)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}