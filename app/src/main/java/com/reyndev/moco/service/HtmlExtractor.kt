package com.reyndev.moco.service

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.reyndev.moco.model.Article
import it.skrape.core.document
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.h1
import it.skrape.selects.html5.p

private const val EXTRACT_HTML_TAG = "ExtractHTML"

/** Function to scrap HTML from the given link */
suspend fun extractHtml(link: String, ctx: Context): Article? {
    try {
        return skrape(AsyncFetcher) {
            // Set request URL based on specified parameter
            request {
                url = link
            }
            // Get the HTML body and assign it to each variable in Article data class
            response {
                Article(
                    link = link,
                    title = document.h1 { findFirst { text } },     // Get the text of the first <H1> tag
                    desc =  document.p {              // Get every <p> text
                        findAll {
                            eachText
                                .filter { it.length > 50 }  // Get text where length is more than 50
                                .subList(0, 3)              // Take only first three element
                        }
                    }.toString(),
                    tags = null,   // Null for now, will be assigned later
                    date = null,   // Null for now, will be assigned later
                )
            }
        }
    } catch (e: Exception) {
        Log.wtf(EXTRACT_HTML_TAG, "Failed to parse HTML")
        e.printStackTrace()
        Toast.makeText(ctx, "Failed to read the link", Toast.LENGTH_SHORT).show()
        return null
    }
}