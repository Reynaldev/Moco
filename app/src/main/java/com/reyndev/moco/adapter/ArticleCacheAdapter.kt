package com.reyndev.moco.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reyndev.moco.ArticleActivity
import com.reyndev.moco.ArticleActivityType
import com.reyndev.moco.R
import com.reyndev.moco.databinding.ArticleListItemBinding
import com.reyndev.moco.model.Article
import com.reyndev.moco.viewmodel.ArticleViewModel

private const val TAG = "ArticleCacheAdapter"

enum class ArticleViewHolderButton {
    COPY,
    SHARE,
    DELETE,
    EDIT
}

class ArticleCacheAdapter(
    private val onClick: (Article) -> Unit,
    private val onLongClick: (Article) -> Unit,
    private val onCopy: (Article) -> Unit,
    private val onShare: (Article) -> Unit,
    private val onDelete: (Article) -> Unit,
    private val onEdit: (Article) -> Unit
) : ListAdapter<Article, ArticleCacheAdapter.ArticleViewHolder>(DiffCallback) {

    class ArticleViewHolder(private val binding: ArticleListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        var show = false

        @SuppressLint("SimpleDateFormat")
        fun bind(article: Article) {
            binding.title.text = article.title
            binding.desc.text = article.desc
            binding.date.text = SimpleDateFormat("dd/MM/yyyy")
                .format(article.date?.toLong())
        }

        /**
         * Show details for the view
         * Try it by long tap a view
         */
        fun showDetail() {
            show = !show

            when (show) {
                true -> binding.details.visibility = View.VISIBLE
                false -> binding.details.visibility = View.GONE
            }
        }

        /**
        * Bind a button to a click listener specifically
        */
        fun bindButton(
            article: Article,
            btn: ArticleViewHolderButton,
            clickListener: (Article) -> Unit
        ) {
            when (btn) {
                ArticleViewHolderButton.COPY -> binding.copy.setOnClickListener {
                    clickListener(article)
                }
                ArticleViewHolderButton.SHARE -> binding.share.setOnClickListener {
                    clickListener(article)
                }
                ArticleViewHolderButton.DELETE -> binding.delete.setOnClickListener {
                    clickListener(article)
                }
                ArticleViewHolderButton.EDIT -> binding.edit.setOnClickListener {
                    clickListener(article)
                }
            }
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

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = getItem(position)
        val itemView = holder.itemView

        /** Bind click listener to onClick variable */
        itemView.setOnClickListener {
//            Toast.makeText(ctx, item.title, Toast.LENGTH_SHORT).show()
            onClick(item)
        }

        itemView.setOnLongClickListener {
            onLongClick(item)
            holder.showDetail()

            Log.v(TAG, "Item position: $position")

            true
        }

        /**
        * Bind every button
        */
        holder.bindButton(item, ArticleViewHolderButton.COPY, onCopy)
        holder.bindButton(item, ArticleViewHolderButton.SHARE, onShare)
        holder.bindButton(item, ArticleViewHolderButton.DELETE, onDelete)
        holder.bindButton(item, ArticleViewHolderButton.EDIT, onEdit)

        /**
        * Assign margin to the view.
        *
        * The reason we're doing this is because there's a bug where the margin is not set in
        * layout xml (article_list_item.xml).
        *
        * If it's the last item, we're gonna add more bottom margin so the view doesn't
        * get blocked by "Add" FloatingActionButton.
        *
        * Note:
        * This will cause a bug where the added item will be far below
        * the item with multiplied margin (In this case is the last item in position).
        * To fix this, just restart the app and it will fixed itself
        * */
        val marginValue = 50
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            .also { it.setMargins(marginValue, marginValue, marginValue, 0) }
        itemView.layoutParams = layoutParams

        if (position == (itemCount - 1)) {
            layoutParams.setMargins(marginValue, marginValue, marginValue, marginValue * 10)
            itemView.layoutParams = layoutParams
        }

        // Bind item to view
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