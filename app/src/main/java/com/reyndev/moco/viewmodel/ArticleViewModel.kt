package com.reyndev.moco.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.reyndev.moco.model.Article
import com.reyndev.moco.model.ArticleDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "ArticleViewModel"

class ArticleViewModel(private val dao: ArticleDao) : ViewModel() {
    private fun insert(article: Article) {
        viewModelScope.launch {
            dao.insert(article)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun insertArticle(link: String, title: String, desc: String, tags: String) {
        val date = Date().time.toString()
        val article = Article(link, title, desc, date, tags.lowercase(Locale.ROOT))

        insert(article)

//        Log.v(TAG, dateStr.toString())
    }
}

class ArticleViewModelFactory(private val dao: ArticleDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleViewModel(dao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}