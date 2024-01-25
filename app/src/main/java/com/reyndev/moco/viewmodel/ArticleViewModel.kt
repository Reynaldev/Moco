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
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.Locale

private const val TAG = "ArticleViewModel"

class ArticleViewModel(private val dao: ArticleDao) : ViewModel() {
    private val _search = MutableLiveData<String?>()
    val search: LiveData<String?> = _search

//    private val _tags = MutableLiveData<List<String>>()

    /**
     * This shouldn't be modified, it's used as a copy.
     * See more inside the [getArticlesByFilter] method in this class
     *
     * @see getArticlesByFilter
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
    private suspend fun syncDatabase(data: List<Article>) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "Writing to local database")

            /**
             * If the local database is empty, insert the articles.
             * Otherwise, compare each article
             * */
            if (articles.value!!.isEmpty()) {
                for (article: Article in data) dao.insert(article)
            } else {
                /**
                 * See if every article is exist in local database.
                 * If no matching found, insert it. Otherwise, do nothing.
                 * */
                for (article: Article in data) {
                    articles.value?.indexOf(article).let {
                        if (it == null || it < 0) dao.insert(article)
                    }
                }
            }
        }.join()

        Log.i(TAG, "Finished writing to local database")
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
     * The given value then will be used as a filter to [articles] LiveData
     * */
    fun setSearch(input: String?) {
        _search.value = input
    }

    /**
     * Insert an article with given params.
     * Will return a Boolean to indicate as a result.
     *
     * @return [Boolean]
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
     * @return [Boolean]
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
     *
     * @return [Boolean]
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
     * return articles list as a LiveData based on [search] value
     *
     * @see [search]
     * @return [LiveData]
     * */
    fun getArticlesByFilter(): LiveData<List<Article>> {
        return articles.map { articleList ->
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
    }

    /**
     * Return an article with given URL
     *
     * @return [LiveData]
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
    suspend fun syncFromDatabase(db: FirebaseDatabase, auth: FirebaseAuth) {
        /** Skip if the user is not signed in */
        if (auth.currentUser == null) {
            Log.w(TAG, "User is not signed in")
            return
        }

        try {
            Log.i(TAG, "Synchronizing from FirebaseDatabase")

            /**
             * Get a response from FirebaseDatabase
             * */
            val result = db.reference.child(auth.currentUser!!.uid)
                .child("articles")
                .child("value")
                .get()
                .await()

            syncDatabase(firebaseJsonToArticles(result.value))

            Log.i(TAG, "Successfully synchronized from FirebaseDatabase")
        } catch (e: Exception) {
            Log.wtf(TAG, "Failed to connect with FirebaseDatabase")
            e.printStackTrace()
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

        try {
            Log.i(TAG, "Synchronizing to FirebaseDatabase")

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

    /**
     * Used to DELETE all record from article table in [ArticleDatabase]. Use with caution
     * */
    fun deleteAllArticle() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAllArticle()
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