package com.reyndev.moco.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reyndev.moco.ArticleActivity
import com.reyndev.moco.ArticleActivityType
import com.reyndev.moco.databinding.ArticleListItemBinding
import com.reyndev.moco.model.Article
import com.reyndev.moco.viewmodel.ArticleViewModel

private const val TAG = "ArticleCacheAdapter"

class ArticleCacheAdapter(private val ctx: Context)
    : ListAdapter<Article, ArticleCacheAdapter.ArticleViewHolder>(DiffCallback) {

    class ArticleViewHolder(
            private val binding: ArticleListItemBinding,
            private val ctx: Context
        ) : RecyclerView.ViewHolder(binding.root) {

        var show = false

        @SuppressLint("SimpleDateFormat")
        fun bind(article: Article) {
            binding.title.text = article.title
            binding.desc.text = article.desc
            binding.date.text = SimpleDateFormat("dd/MM/yyyy")
                .format(article.date?.toLong())

            binding.delete.setOnClickListener {

            }

            binding.edit.setOnClickListener {
                val intent = Intent(ctx, ArticleActivity::class.java)
                intent.putExtra(ArticleActivity.EXTRA_TYPE, ArticleActivityType.EDIT.name)
                intent.putExtra(ArticleActivity.EXTRA_ARTICLE, article.id)
                ctx.startActivity(intent)
            }
        }

        /*
        * Show details for the view, by showing edit button and share button
        * Try it by long tap a view
         */
        fun showDetail() {
            show = !show

            when (show) {
                true -> binding.details.visibility = View.VISIBLE
                false -> binding.details.visibility = View.GONE
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
            ),
            ctx
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = getItem(position)
        val itemView = holder.itemView

        /* Bind click listener to onClick variable */
        itemView.setOnClickListener {
            Toast.makeText(ctx, item.title, Toast.LENGTH_SHORT).show()
        }

        itemView.setOnLongClickListener {
            holder.showDetail()

            true
        }

        /*
        * Assign margin to the view.
        *
        * The reason we're doing this is because there's a bug where the margin is not set in
        * layout xml (article_list_item.xml).
        *
        * If it's the last item, we're gonna add more bottom margin so the view doesn't
        * get blocked by "Add" FloatingActionButton.
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