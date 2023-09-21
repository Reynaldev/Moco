package com.reyndev.moco.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article")
data class Article(
    @PrimaryKey val link: String,
    val title: String?,
    val desc: String?,
    val date: String?,
    val tags: String?
)
