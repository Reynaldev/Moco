package com.reyndev.moco

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
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

/*
* Debugging tag, please don't remove.
* Rule applies to every class that has it
*/
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
    private lateinit var adapter: ArticleCacheAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        * BottomSheetBehavior for the NavigationView,
        * try it by tapping on user icon (top-right) button,
        * Drag down the view or tap the close button to close.
        * */
        bottomSheetBehavior = BottomSheetBehavior.from(binding.navigationView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Initialize Firebase
        db = Firebase.database
        auth = Firebase.auth

        /*
        * Better get MenuItem
        *
        * [Previous] Hardcoded MenuItem -> binding.navigationView.menu[1]
         */
        val miSignUser = binding.navigationView.menu.findItem(R.id.si_sign_user)

        if (auth.currentUser == null) {
            binding.displayName.text = getString(R.string.username, "Anon")
            miSignUser.title = "Sign In"
//            binding.btnSettings.setImageDrawable(getDrawable(R.drawable.ic_account_circle))
        } else {
            binding.displayName.text = getString(R.string.username, auth.currentUser?.displayName)
            miSignUser.title = "Sign Out"
//            binding.btnSettings.setImageURI(auth.currentUser!!.photoUrl)
        }

        // Adapter and RecyclerView setup
        adapter = ArticleCacheAdapter(
            /* onClick */
            {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.link))
                startActivity(intent)

//                Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
            },
            /* onLongClick */
            {},
            /* onShare | On share button clicked */
            {
                /*
                * Try implement a share function
                */
                try {
                    /*
                    * Create the intent with action Intent.ACTION_SEND
                    * with data of the current article link
                    * and type of "text/plain"
                    */
                    val intentShare = Intent(Intent.ACTION_SEND)
                        .apply {
                            putExtra(Intent.EXTRA_TEXT, it.link)
                            type = "text/plain"
                        }

                    /*
                    * Show a chooser to let the user choose which app he/she want to use
                    */
                    val intentChooser = Intent.createChooser(intentShare, "Share this article")

                    /*
                    * Start the intent, try it by tapping the share button
                    */
                    startActivity(intentChooser)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "No suitable app found", Toast.LENGTH_SHORT).show()
                }

//                Toast.makeText(this, "Shared", Toast.LENGTH_SHORT).show()
            },
            /* onDelete | On delete button clicked */
            {
                viewModel.deleteArticle(it)

                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            },
            /* onEdit | On edit button clicked */
            {
                val intent = Intent(this, ArticleActivity::class.java)
                intent.putExtra(ArticleActivity.EXTRA_TYPE, ArticleActivityType.EDIT.name)
                intent.putExtra(ArticleActivity.EXTRA_ARTICLE, it.id)
                startActivity(intent)
            }
        )
        binding.recyclerView.adapter = adapter
        updateAdapter()

        /*
        * Observe the search variable of ArticleViewModel, so we can update the adapter
        * based on text searched. See more inside the ArticleViewModel
        */
        viewModel.search.observe(this) {
            updateAdapter()
        }

        // Search field text changed listener
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearch(binding.etSearch.text.toString())
            }
        })

        binding.btnSettings.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, ArticleActivity::class.java)
            intent.putExtra(ArticleActivity.EXTRA_TYPE, ArticleActivityType.ADD.name)
            startActivity(intent)
        }

        // Events based on which MenuItem is clicked
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

    // Sign out and restart activity
    private fun signOut() {
        auth.signOut()
        finish()
        startActivity(intent)
    }

    // Synchronize cache database with FirebaseDatabase
    private fun syncDatabase(search: String?) {

    }

    // Update ArticleCacheAdapter
    private fun updateAdapter() {
        viewModel.getArticles().observe(this) {
            adapter.submitList(it)
        }
    }
}