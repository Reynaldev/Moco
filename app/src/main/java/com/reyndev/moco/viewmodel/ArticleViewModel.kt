package com.reyndev.moco.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.reyndev.moco.model.Article
import com.reyndev.moco.model.ArticleDao
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

private const val TAG = "ArticleViewModel"

class ArticleViewModel(private val dao: ArticleDao) : ViewModel() {
    private val _search = MutableLiveData<String?>()
    val search: LiveData<String?> = _search

    private val _tags = MutableLiveData<List<String>>()

    private var articles = dao.getArticles().asLiveData()

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

    fun setSearch(input: String?) {
        _search.value = input
    }

    fun getArticles(): LiveData<List<Article>> {
        // Reassign articles, rather than calling ArticleDao everytime or using the same variable
        // since both ways are the same
        val query = articles.map { articleList ->
            // Check if search is empty or null
            if (!search.value.isNullOrBlank()) {
                // Filter only matching titles
                articleList.filter {
                    it.title!!.contains(search.value!!, true)
                }
            } else {
                articleList
            }
        }

        return query
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