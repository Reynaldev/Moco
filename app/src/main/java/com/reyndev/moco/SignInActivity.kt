package com.reyndev.moco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "SignInActivity"

class SignInActivity : AppCompatActivity() {

    private val signIn: ActivityResultLauncher<Intent> = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(), ::onSignInResult
    )

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()

        /*
        * Show user the Authentication UI and Email provider
        * Otherwise, go to MainActivity
         */
        if (auth.currentUser == null) {
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.Theme_Moco)
                .setAvailableProviders(listOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                ))
                .build()

            signIn.launch(signInIntent)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    /*
    * Callback function to get the authentication result
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Sign in success", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "There was an error signing in", Toast.LENGTH_SHORT).show()

            val response = result.idpResponse
            if (response == null) {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Sign in error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}