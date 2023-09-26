package com.reyndev.moco

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.coroutineScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.reyndev.moco.databinding.ActivityArticleBinding
import com.reyndev.moco.model.Article
import com.reyndev.moco.service.extractHtml
import com.reyndev.moco.viewmodel.ArticleViewModel
import com.reyndev.moco.viewmodel.ArticleViewModelFactory
import kotlinx.coroutines.launch

private const val TAG = "ArticleActivity"

enum class ArticleActivityType {
    ADD,
    EDIT
}

class ArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleBinding
//    private lateinit var activityType: ArticleActivityType

    private val viewModel: ArticleViewModel by viewModels {
        ArticleViewModelFactory(
            (application as MocoApplication).database.articleDao()
        )
    }

    private lateinit var db: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Intent
        * Get the ArticleActivityType enum name from the intent
        * And assign it to the local variable
        */
//        val activityType = ArticleActivityType.valueOf(intent.extras?.getString(EXTRA_TYPE)!!)
//        val articleId = intent.extras?.getInt(EXTRA_ARTICLE)

//        Log.v(TAG, "Intent: ${intent}\nType: ${activityType}")

        db = Firebase.database
        auth = Firebase.auth

        binding.apply {
            /*
            * Get intent from other app.
            * Otherwise, get intent from previous Activity
            */
            if (intent?.action == Intent.ACTION_SEND) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    assignIntoView(it)
                }

                btnSubmit.setOnClickListener {
                    if (isValidInput())
                        insertArticle()
                }
            } else {
                val activityType = ArticleActivityType.valueOf(intent.extras?.getString(EXTRA_TYPE)!!)
                val articleId = intent.extras?.getInt(EXTRA_ARTICLE)

                when (activityType) {
                    /* Show this if we want to ADD an article */
                    ArticleActivityType.ADD -> {
                        val articleLink = intent.extras?.getString(EXTRA_LINK)
                        if (!articleLink.isNullOrEmpty())
                            assignIntoView(articleLink)

                        btnSubmit.setOnClickListener {
                            if (isValidInput())
                                insertArticle()
                        }

                        showLinkInputDialog()
                    }
                    /* Show this if we want to EDIT an article */
                    ArticleActivityType.EDIT -> {
                        /* Get the article from the viewmodel */
                        viewModel.getArticleSpecified(articleId!!)
                            .observe(this@ArticleActivity) { article ->
                                /* Bind to each TextField */
                                etLink.setText(article.link)
                                etTitle.setText(article.title)
                                etTags.setText(article.tags)
                                etDesc.setText(article.desc)

                                /*
                            * Submit the edited article into the viewmodel, if
                            * only the input is valid.
                            */
                                btnSubmit.setOnClickListener {
                                    if (isValidInput()) {
                                        /* Update */
                                        if (viewModel.updateArticle(
                                                Article(
                                                    article.id,
                                                    etLink.text.toString(),
                                                    etTitle.text.toString(),
                                                    etDesc.text.toString(),
                                                    article.date,
                                                    etTags.text.toString(),
                                                )
                                            )
                                        ) {
                                            // If the update is success
                                            Toast.makeText(
                                                this@ArticleActivity,
                                                "Article updated",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        } else {
                                            // If the update is failed
                                            Toast.makeText(
                                                this@ArticleActivity,
                                                "Failed to update article",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        finish()
                                    }
                                }
                            }

                        btnSubmit.setText(R.string.btn_submit_edit)
                    }
                }
            }

            /* For views and variables whether it's in EDIT or ADD mode */

            elLink.setEndIconOnClickListener {
                showLinkInputDialog()
            }

            btnCancel.setOnClickListener {
                finish()
            }
        }
    }

    /* Show link insert MaterialDialog */
    private fun showLinkInputDialog() {
        // EditText to put inside the Dialog
        val input = EditText(this)
            .also {
                it.hint = "Put the link in here"
            }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add from link")
            .setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                dialog.dismiss()
                assignIntoView(input.text.toString())
            }
            .show()
    }

    private fun assignIntoView(link: String) {
        lifecycle.coroutineScope.launch {
            Log.v(TAG, "Reading..")

            toggleLoading(true)

            val article = extractHtml(link, applicationContext)

            binding.etLink.setText(article?.link)
            binding.etTitle.setText(article?.title)
            binding.etDesc.setText(article?.desc)

            toggleLoading(false)

            Log.v(TAG, "Finished")
        }
    }

    private fun isValidInput(): Boolean {
        binding.apply {
            if (etLink.text.isNullOrEmpty()) {
                etLink.error = "This can't be empty"
                return false
            }

            if (etTitle.text.isNullOrEmpty()) {
                etTitle.error = "This can't be empty"
                return false
            }

            if (etTags.text.isNullOrEmpty()) {
                etTags.error = "This can't be empty"
                return false
            }

            if (etDesc.text.isNullOrEmpty()) {
                etDesc.error = "This can't be empty"
                return false
            }

            return true
        }
    }

    private fun insertArticle() {
        binding.apply {
            viewModel.insertArticle(
                etLink.text.toString(),
                etTitle.text.toString(),
                etDesc.text.toString(),
                etTags.text.toString()
            )

            finish()
        }
    }

    private fun toggleLoading(visible: Boolean) {
        when (visible) {
            true -> binding.loading.visibility = View.VISIBLE
            false -> binding.loading.visibility = View.GONE
        }
    }

    companion object {
        const val EXTRA_TYPE = "detail"
        const val EXTRA_ARTICLE = "article"
        const val EXTRA_LINK = "link"
    }
}