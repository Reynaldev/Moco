package com.reyndev.moco.service

import android.util.Log
import com.reyndev.moco.model.Article
import it.skrape.core.document
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.h1
import it.skrape.selects.html5.p
import it.skrape.selects.text
import kotlin.text.Typography.paragraph

private const val EXTRACT_HTML_TAG = "ExtractHTML"

/* github: https://github.com/skrapeit/skrape.it */
// Function to scrap HTML from the given link
fun extractHtml(link: String): Article? {
    return skrape(HttpFetcher) {
        // Set request URL based on specified parameter
        request {
            url = link
        }
        // Get the HTML body and assign it to each variable in Article data class
        response {
            Article(
                link,
                document.h1 { findFirst { text } },     // Get the text of the first <H1> tag
                document.p {              // Get every <p> text
                    findAll {
                        eachText
                            .filter { it.length > 50 }
                            .subList(0, 3)
                    }
                }.toString(),
                null,   // Null for now, will be assigned later
                null,   // Null for now, will be assigned later
            )
        }
    }
}

/* Testing */
//fun extractHtml_test(link: String) {
//    lateinit var pg: List<String>        //Paragraph
//    skrape(HttpFetcher) {
//        // Set request URL based on specified parameter
//        request {
//            url = link
//        }
//        // Get the HTML body and assign it to each variable in Article data class
//        response {
//            pg = document.p {               // Get every <p> text
//                findAll {
//                    eachText
//                        .filter { it.length > 50 }        // Get text where length is more than 50
//                        .subList(0, 3)                    // SubList to only 3 elements
//                }
//            }
//        }
//    }
//
//    Log.v(EXTRACT_HTML_TAG, pg.size.toString())
//}