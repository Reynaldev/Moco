package com.reyndev.moco

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.reyndev.moco.databinding.ActivityAddArticleBinding
import com.reyndev.moco.model.Article

class AddArticleActivity : AppCompatActivity() {
    private val TAG = "AddArticleActivity"
    private val ART = "articles"

    private lateinit var binding: ActivityAddArticleBinding

    private lateinit var db: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.database
        auth = Firebase.auth

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnAdd.setOnClickListener {
            val link = binding.etLink.text.toString()
            val title = binding.etTitle.text.toString()
            val tags = binding.etTags.text.toString()
                .splitToSequence(",", " ")
                .filter { it.isNotEmpty() }
                .toList()

            val desc = binding.etDesc.text.toString()

            // TODO: Insert into Firebase RealtimeDatabase
        }
    }
}