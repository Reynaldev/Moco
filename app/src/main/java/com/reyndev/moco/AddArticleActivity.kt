package com.reyndev.moco

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.coroutineScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.reyndev.moco.databinding.ActivityAddArticleBinding
import com.reyndev.moco.model.Article
import com.reyndev.moco.service.extractHtml
import com.reyndev.moco.viewmodel.ArticleViewModel
import com.reyndev.moco.viewmodel.ArticleViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "AddArticleActivity"
private const val ART = "articles"

class AddArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddArticleBinding

    private val viewModel: ArticleViewModel by viewModels {
        ArticleViewModelFactory(
            (application as MocoApplication).database.articleDao()
        )
    }

    private lateinit var db: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.database
        auth = Firebase.auth

        binding.elLink.setEndIconOnClickListener {
            showLinkInputDialog()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnAdd.setOnClickListener {
            insertArticle()
        }

        showLinkInputDialog()
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

//                if (assignIntoView(extractHtml(input.text.toString(), this))) {
//                    Toast.makeText(this, "Success reading the link", Toast.LENGTH_SHORT).show()
//                }
//
//                dialog.dismiss()

                /* Testing */
//                extractHtml_test(input.text.toString())
//                dialog.dismiss()
            }
            .show()
    }

    /* Assign variable to each TextInputEditText */
//    private fun assignIntoView(article: Article?): Boolean {
//        if (article == null)
//            return false
//
//        binding.etLink.setText(article.link)
//        binding.etTitle.setText(article.title)
//        binding.etDesc.setText(article.desc)
//
//        return true
//    }

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

    private fun insertArticle() {
        val link = binding.etLink
        val title = binding.etTitle
        val tags = binding.etTags
        val desc = binding.etDesc

        if (link.text.isNullOrEmpty()) {
            link.error = "This can't be empty"
            return
        }

        if (title.text.isNullOrEmpty()) {
            title.error = "This can't be empty"
            return
        }

        if (tags.text.isNullOrEmpty()) {
            tags.error = "This can't be empty"
            return
        }

        if (desc.text.isNullOrEmpty()) {
            desc.error = "This can't be empty"
            return
        }

        viewModel.insertArticle(
            link.text.toString(),
            title.text.toString(),
            desc.text.toString(),
            tags.text.toString()
        )

        finish()
    }

    fun toggleLoading(visible: Boolean) {
        when (visible) {
            true -> binding.loading.visibility = View.VISIBLE
            false -> binding.loading.visibility = View.GONE
        }
    }
}