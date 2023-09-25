package com.reyndev.moco.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.reyndev.moco.model.Article
import com.reyndev.moco.model.ArticleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

private const val TAG = "ArticleViewModel"

class ArticleViewModel(private val dao: ArticleDao) : ViewModel() {
    private val _search = MutableLiveData<String?>()
    val search: LiveData<String?> = _search

//    private val _tags = MutableLiveData<List<String>>()

    // This shouldn't be modified, it's used as a copy.
    // See more inside the getArticles() method in this class
    private val articles = dao.getArticles().asLiveData()

    private fun getArticleAsObject(link: String, title: String, desc: String, tags: String): Article {
        return Article(
            link = link,
            title = title,
            desc = desc,
            date = Date().time.toString(),
            tags = tags.lowercase(Locale.ROOT)
        )
    }

    fun setSearch(input: String?) {
        _search.value = input
    }

    fun insertArticle(link: String, title: String, desc: String, tags: String): Boolean {
        return try {
            viewModelScope.launch(Dispatchers.IO) {
                dao.insert(getArticleAsObject(link, title, desc, tags))
            }

            true
        } catch (e: Exception) {
            Log.wtf(TAG, "Cannot insert ${title}")
            e.printStackTrace()
            false
        }
    }

    fun updateArticle(article: Article): Boolean {
        return try {
            viewModelScope.launch(Dispatchers.IO) {
                dao.update(article)
            }

            true
        } catch (e: Exception) {
            Log.wtf(TAG, "Cannot update ${article.title} with id of ${article.id}")
            e.printStackTrace()
            false
        }
    }

    fun deleteArticle(article: Article): Boolean {
        return try {
            viewModelScope.launch(Dispatchers.IO) {
                dao.delete(article)
            }

            true
        } catch (e: Exception) {
            Log.wtf(TAG, "Cannot delete ${article.title} with id of ${article.id}")
            e.printStackTrace()
            false
        }
    }

    fun getArticles(): LiveData<List<Article>> {
        // Reassign articles, rather than calling ArticleDao everytime or using the same variable
        // since both ways are the same.
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

    fun getArticleSpecified(id: Int): LiveData<Article> {
        return dao.getArticleSpecified(id).asLiveData()
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