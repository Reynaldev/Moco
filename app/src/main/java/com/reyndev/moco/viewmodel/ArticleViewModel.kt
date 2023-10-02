package com.reyndev.moco.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.reyndev.moco.model.Article
import com.reyndev.moco.model.ArticleDao
import com.reyndev.moco.service.firebaseJsonToArticles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

private const val TAG = "ArticleViewModel"

class ArticleViewModel(private val dao: ArticleDao) : ViewModel() {
    private val _search = MutableLiveData<String?>()
    val search: LiveData<String?> = _search

//    private val _tags = MutableLiveData<List<String>>()

    /**
     * This shouldn't be modified, it's used as a copy.
     * See more inside the [getArticles] method in this class
     *
     * @see getArticles
     */
    private val articles = dao.getArticles().asLiveData()

    /**
     * Method to sync database from given param,
     * only use this to sync from FirebaseDatabase
     *
     * @see syncFromDatabase
     * @see syncToDatabase
     * @see firebaseJsonToArticles
     * */
    private fun syncDatabase(data: MutableList<Article>) {
        viewModelScope.launch {
            data.forEach {
                Log.v(TAG, "Article: $it")
                dao.insert(it)
            }
        }
    }

    /**
     * Convert given input into an Article class object
     * */
    private fun getArticleAsObject(link: String, title: String, desc: String, tags: String): Article {
        return Article(
            link = link,
            title = title,
            desc = desc,
            date = Date().time.toString(),
            tags = tags.lowercase(Locale.ROOT)
        )
    }

    /**
     * Set search value with given parameter.
     * The given value then will be used as a filter to articles LiveData
     * */
    fun setSearch(input: String?) {
        _search.value = input
    }

    /**
     * Insert an article with given params.
     * Will return a Boolean to indicate as a result.
     *
     * @return Boolean
     * */
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

    /**
     * Update an article with given param.
     * Will return a Boolean to indicate as a result.
     *
     * @return Boolean
     * */
    fun updateArticle(article: Article): Boolean {
        return try {
            viewModelScope.launch(Dispatchers.IO) {
                dao.update(article)
            }

            true
        } catch (e: Exception) {
            Log.wtf(TAG, "Cannot update ${article.title}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Delete an article with given param.
     * Will return a Boolean to indicate as a result
     * */
    fun deleteArticle(article: Article): Boolean {
        return try {
            viewModelScope.launch(Dispatchers.IO) {
                dao.delete(article)
            }

            true
        } catch (e: Exception) {
            Log.wtf(TAG, "Cannot delete ${article.title}")
            e.printStackTrace()
            false
        }
    }

    /**
     * return articles list as a LiveData
     *
     * @return LiveData
     * */
    fun getArticles(): LiveData<List<Article>> {
        /**
         * Reassign articles, rather than calling ArticleDao everytime or using the same variable
         * since both ways are the same.
         */
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

    /**
     * Return an article with given URL
     *
     * @return LiveData
     * */
    fun getArticleSpecified(link: String): LiveData<Article> {
        return dao.getArticleByLink(link).asLiveData()
    }

    /**
     * Sync from FirebaseDatabase into local database,
     * it will get the JSON response as an [Any],
     * send it to [firebaseJsonToArticles] for parsing,
     * and then will be passed to [syncDatabase].
     *
     * @see syncDatabase
     * @see firebaseJsonToArticles
     * */
    fun syncFromDatabase(db: FirebaseDatabase, auth: FirebaseAuth) {
        /** Skip if the user is not signed in */
        if (auth.currentUser == null) {
            Log.w(TAG, "User is not signed in")
            return
        }

        /**
         * Run inside a coroutine
         *
         * Why?
         *
         * Well, taking response from the internet can take a while
         * and that will cause unresponsive behavior.
         *
         * You should have know that.
         * */
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i(TAG, "Synchronizing to FirebaseDatabase")

                /**
                 * Get a response from FirebaseDatabase
                 * */
                db.reference.child(auth.currentUser!!.uid)
                    .child("articles")
                    .child("value")
                    .get()
                    .addOnSuccessListener {
                        /** Sync */
                        syncDatabase(firebaseJsonToArticles(it.value))
//                        Log.v(TAG, "JSON: ${it.value}")
                    }

                Log.i(TAG, "Successfully connected to FirebaseDatabase")
            } catch (e: Exception) {
                Log.wtf(TAG, "Failed to connect with FirebaseDatabase")
                e.printStackTrace()
            }
        }
    }

    /**
     * A function to sync local database to FirebaseDatabase
     * */
    fun syncToDatabase(db: FirebaseDatabase, auth: FirebaseAuth) {
        /** Skip if the user is not signed in */
        if (auth.currentUser == null) {
            Log.w(TAG, "User is not signed in")
            return
        }

        /** Run inside a coroutine */
        viewModelScope.launch {
            try {
                /**
                 * Set the value of FirebaseDatabase from user uid child
                 * with [articles].
                 * */
                db.reference.child(auth.currentUser!!.uid)
                    .child("articles")
                    .setValue(articles)

                Log.i(TAG, "Successfully synchronized to FirebaseDatabase")
            } catch (e: Exception) {
                Log.wtf(TAG, "Failed to sync with FirebaseDatabase")
                e.printStackTrace()
            }
        }
    }
}

/**
 * Custom ViewModelFactory, this is necessary so the ViewModel is not related with
 * certain Activity's lifecycle
 * */
class ArticleViewModelFactory(private val dao: ArticleDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleViewModel(dao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}