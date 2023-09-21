package com.reyndev.moco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.reyndev.moco.adapter.ArticleCacheAdapter
import com.reyndev.moco.databinding.ActivityMainBinding
import com.reyndev.moco.viewmodel.ArticleViewModel
import com.reyndev.moco.viewmodel.ArticleViewModelFactory

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: ArticleViewModel by viewModels {
        ArticleViewModelFactory(
            (application as MocoApplication).database.articleDao()
        )
    }

    private lateinit var db: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NavigationView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.navigationView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        db = Firebase.database
        auth = Firebase.auth

        if (auth.currentUser == null) {
            binding.displayName.text = getString(R.string.username, "Anon")
//            binding.btnSettings.setImageDrawable(getDrawable(R.drawable.ic_account_circle))
        } else {
            binding.displayName.text = getString(R.string.username, auth.currentUser?.displayName)
//            binding.btnSettings.setImageURI(auth.currentUser!!.photoUrl)
        }

        binding.recyclerView.adapter = ArticleCacheAdapter { article ->
            Log.v(TAG, article.title.toString())
        }

        binding.btnSettings.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.si_close -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                R.id.si_sign_user -> {
                    if (auth.currentUser == null) {
                        signIn()
                    } else {
                        signOut()
                    }
                }
                else -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

            true
        }
    }

    override fun onResume() {
        super.onResume()

        // TODO: Sync with FirebaseDatabase if user is not anon

    }

    override fun onStop() {
        super.onStop()

        // TODO: Sync with FirebaseDatabase if user is not anon
    }

    // Start SignInActivity to SignIn the user
    private fun signIn() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    // Restart activity when the user is SignOut
    private fun signOut() {
        auth.signOut()
        startActivity(intent)
        finish()
    }

    // Synchronize cache database with FirebaseDatabase
    private fun syncDatabase() {

    }
}