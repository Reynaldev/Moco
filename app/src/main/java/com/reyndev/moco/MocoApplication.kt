package com.reyndev.moco

import android.app.Application
import com.reyndev.moco.model.ArticleDatabase

/**
 * Custom base application derived from [Application].
 * Used as a place to initialize [lazy] object that needs it's own lifecycle.
 * */
class MocoApplication : Application() {
    val database: ArticleDatabase by lazy { ArticleDatabase.getDatabase(this) }
}