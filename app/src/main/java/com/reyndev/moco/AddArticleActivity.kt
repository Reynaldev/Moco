package com.reyndev.moco

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.reyndev.moco.databinding.ActivityAddArticleBinding

class AddArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

//            Log.v(TAG, "link: $link\ntitle: $title\ntags: ${tags}\ndesc: $desc")
            tags.forEach {
                Log.v(TAG, "$it")
            }
        }
    }

    companion object {
        const val TAG = "AddArticleActivity"
    }
}