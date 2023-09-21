package com.reyndev.moco

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
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
//            insertArticle()
        }
    }

    override fun onStart() {
        super.onStart()

        showLinkInputDialog()
    }

    private fun showLinkInputDialog() {
        val input = EditText(this)
            .also {
                it.hint = "Put the link in here"
            }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add from link")
            .setView(input)
            .setPositiveButton("Add") { dialog, type ->
                if (assignIntoView(extractHtml(input.text.toString(), this))) {
                    Toast.makeText(this, "Success reading the link", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()

                /* Testing */
//                extractHtml_test(input.text.toString())
//                dialog.dismiss()
            }
            .show()
    }

    private fun assignIntoView(article: Article?): Boolean {
        if (article == null)
            return false

        binding.etLink.setText(article.link)
        binding.etTitle.setText(article.title)
        binding.etDesc.setText(article.desc)

        return true
    }

    private fun insertArticle() {
        // TODO: Insert into Cache Database (ArticleDatabase)
    }
}