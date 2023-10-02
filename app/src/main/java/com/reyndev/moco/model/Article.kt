package com.reyndev.moco.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article")
data class Article(
//    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @PrimaryKey
    val link: String,
    val title: String?,
    val desc: String?,
    val date: String?,
    val tags: String?
)
