package com.reyndev.moco.adapter

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reyndev.moco.databinding.ArticleListItemBinding
import com.reyndev.moco.model.Article

class ArticleCacheAdapter(val onClick: (Article) -> Unit)
    : ListAdapter<Article, ArticleCacheAdapter.ArticleViewHolder>(DiffCallback) {

    class ArticleViewHolder(private val binding: ArticleListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(article: Article) {
            binding.title.text = article.title
            binding.desc.text = article.desc
            binding.date.text = SimpleDateFormat("dd MMM yyyy").format(article.date)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ArticleViewHolder {
        return ArticleViewHolder(
            ArticleListItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: ArticleCacheAdapter.ArticleViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.link == newItem.link
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }
    }
}