package com.reyndev.moco

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

    /**
     * BottomSheetBehavior for the [navigationView],
     * try it by tapping on user icon (top-right) button,
     * Drag down the view or tap the close button to close.
     */
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NavigationView>
    private lateinit var adapter: ArticleCacheAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /** BottomSheetBehavior implementation */
        bottomSheetBehavior = BottomSheetBehavior.from(binding.navigationView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Initialize Firebase
        db = Firebase.database
        auth = Firebase.auth

        /**
         * Better get MenuItem
         *
         * Hardcoded MenuItem -> binding.navigationView.menu[1]
         */
        val miSignUser = binding.navigationView.menu.findItem(R.id.si_sign_user)

        if (auth.currentUser == null) {
            binding.displayName.text = getString(R.string.username, "Anon")
            miSignUser.title = "Sign In"
        } else {
            binding.displayName.text = getString(R.string.username, auth.currentUser?.displayName)
            miSignUser.title = "Sign Out"
        }

        // Adapter and RecyclerView setup
        adapter = ArticleCacheAdapter(
            /**
             * onClick
             *
             * @see ArticleCacheAdapter.onClick
             * */
            {
                val intentData = Intent(Intent.ACTION_VIEW, Uri.parse(it.link))
                startActivity(intentData)

//                Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
            },
            /**
             * onCopy | On Copy button clicked
             *
             * @see ArticleCacheAdapter.onCopy
             * */
            {
                /* Create a Clipboard */
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                /* Create a ClipData and copy the article's link */
                val clip = ClipData.newPlainText("URL", it.link)
                /* Set primary clip of the Clipboard as defined ClipData above */
                clipboard.setPrimaryClip(clip)

                Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            /**
             * onShare | On share button clicked
             *
             * @see ArticleCacheAdapter.onShare
             * */
            {
                /**
                 * Try implement a share function
                 */
                try {
                    /**
                     * Format the text that will be shared
                     */
                    val content = getString(
                        R.string.share_article,
                        it.title,
                        it.desc,
                        it.link
                    )

                    /**
                     * Create the intent with action [Intent.ACTION_SEND]
                     * with extras of the current article link and title
                     * and type of "text/plain"
                     */
                    val intentShare = Intent(Intent.ACTION_SEND)
                        .apply {
                            putExtra(Intent.EXTRA_TEXT, content)
                            putExtra(Intent.EXTRA_TITLE, it.title)
                            putExtra(Intent.EXTRA_REFERRER, Uri.parse(it.link))
                            type = "text/plain"
                        }

                    /**
                     * Show a chooser to let the user choose which app he/she want to use
                     * by [Intent.createChooser]
                     * */
                    val intentChooser = Intent.createChooser(intentShare, "Share this article")

                    /**
                     * Show the app chooser, try it by tapping the share button
                     * */
                    startActivity(intentChooser)

//                    Log.v(TAG, content)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "No suitable app found", Toast.LENGTH_SHORT).show()
                }

//                Toast.makeText(this, "Shared", Toast.LENGTH_SHORT).show()
            },
            /**
             * onDelete | On delete button clicked
             *
             * @see ArticleCacheAdapter.onDelete
             * */
            {
                viewModel.deleteArticle(it)

                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            },
            /**
             * onEdit | On edit button clicked
             *
             * @see ArticleCacheAdapter.onEdit
             * */
            {
                val intentData = Intent(this, ArticleActivity::class.java)
                    .apply {
                        putExtra(ArticleActivity.EXTRA_TYPE, ArticleActivityType.EDIT.name)
//                        putExtra(ArticleActivity.EXTRA_ARTICLE, it.id)
                        putExtra(ArticleActivity.EXTRA_ARTICLE, it.link)
                    }

                startActivity(intentData)
            }
        )
        binding.recyclerView.adapter = adapter
        updateAdapter()

        /**
         * Observe the search variable of [ArticleViewModel.search],
         * so we can update [adapter] based on text searched.
         *
         * @see ArticleViewModel.getArticlesByFilter
         * */
        viewModel.search.observe(this) {
            updateAdapter()
        }

        /** Search field text changed listener */
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearch(binding.etSearch.text.toString())
            }
        })

        /** Show the navigationView when account button is clicked */
        binding.btnSettings.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.fabAdd.setOnClickListener {
            val intentData = Intent(this, ArticleActivity::class.java)
            intentData.putExtra(ArticleActivity.EXTRA_TYPE, ArticleActivityType.ADD.name)
            startActivity(intentData)
        }

        /** Events based on which MenuItem is clicked */
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.si_sign_user -> {
                    if (auth.currentUser == null) {
                        signIn()
                    } else {
                        signOut()
                    }
                }

                R.id.si_sync -> syncFromDatabase()
                R.id.si_about -> startActivity(Intent(this, AboutActivity::class.java))
                R.id.si_close -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                else -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

            true
        }
    }

    /** Start SignInActivity to SignIn the user */
    private fun signIn() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    /** Sign out and restart activity */
    private fun signOut() {
        auth.signOut()
        viewModel.deleteAllArticle()
        finish()
        startActivity(intent)
    }

    /** Show a pop up dialog to synchronize data */
    private fun syncFromDatabase() {
        if (auth.currentUser == null) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Warning")
                .setMessage("You need to sign in to sync your data to cloud. Continue?")
                .setPositiveButton("Yes, let me in") { _, _ -> this.signIn() }
                .setNegativeButton("No way!") { dialog, _ -> dialog.dismiss() }
                .show()

            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Warning")
            .setMessage("Do you want to download your previous data from cloud?")
            .setPositiveButton("Of course!") { dialog, _ ->
                startSync(true)
                dialog.dismiss()
            }
            .setNegativeButton("No, only upload") { dialog, _ ->
                startSync(false)
                dialog.dismiss()
            }
            .show()
    }

    /** Start synchronizing data
     * @param downloadData will take the parameter as a decision whether the users wants to download
     * their data or just upload it
     * */
    private fun startSync(downloadData: Boolean) {
        runBlocking {
            launch(Dispatchers.Default) {
                when (downloadData) {
                    true -> viewModel.syncFromDatabase(db, auth, true)
                    false -> viewModel.syncToDatabase(db, auth)
                }
            }
        }
    }

    /** Update ArticleCacheAdapter */
    private fun updateAdapter() {
        viewModel.getArticlesByFilter().observe(this) {
            adapter.submitList(it)
        }
    }
}