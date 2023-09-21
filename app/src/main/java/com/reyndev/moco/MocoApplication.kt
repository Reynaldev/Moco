package com.reyndev.moco

import android.app.Application
import com.reyndev.moco.model.ArticleDatabase

class MocoApplication : Application() {
    val database: ArticleDatabase by lazy { ArticleDatabase.getDatabase(this) }
}