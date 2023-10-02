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
import org.json.JSONArray
import org.json.JSONObject

/**
 * Function to scrap HTML from the given link
 *
 * github: https://github.com/skrapeit/skrape.it
 * */
suspend fun extractHtml(link: String, ctx: Context): Article? {
    val TAG = "ExtractHTML"

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
        Log.wtf(TAG, "Failed to parse HTML")
        e.printStackTrace()
        Toast.makeText(ctx, "Failed to read the link", Toast.LENGTH_SHORT).show()
        return null
    }
}

/**
 * This function is used to parse the JSON retrieved from FirebaseDatabase
 * and then convert it into [MutableList] of [Article]
 * Example of the JSON response:
 *
 * [
 *  {
 *   date:"0981092834",
 *   desc:"Some desc",
 *   id:1,
 *   link:"https://www.com",
 *   tags:"tag1",
 *   title:"some title"
 *  }, ...List goes on...
 * ]
 *
 * @see JSONObject
 * @see JSONArray
 * */
fun firebaseJsonToArticles(obj: Any?): MutableList<Article> {
    /** Debug tag */
    val TAG = "FirebaseJsonToObject"

    /** Skip if no JSON response and return an empty [MutableList] */
    if (obj == null) {
        Log.w(TAG, "No FirebaseDatabase data")
        return mutableListOf()
    }

    /** Try parsing the JSON */
    try {
        Log.v(TAG, "Parsing FirebaseDatabase...")

        /**
         * We initialize the variable, why the response is casted to [ArrayList]?
         * You can try it by [Log] the output of the obj type by "obj::class.simpleName".
         * It will print the type of the obj and tells us that the type is ArrayList.
         *
         * But we're not so sure what type each element is, so we passed an "*"
         * inside the "<>".
         * */
        val dbData = obj as ArrayList<*>
        val articleList: MutableList<Article> = mutableListOf()

        /**
         * Now we're passing the dbData we defined above into the [JSONArray] parameter.
         * This will turn the dbData into a real [ArrayList] object.
         * */
        val articleListJson = JSONArray(dbData)

        /**
         * Now we're going to loop the [ArrayList] and parse each key-value into [Article] object.
         * */
        var i = 0
        while (i < articleListJson.length()) {
            /**
             * Create [JSONObject] from the articleListJson [JSONArray].
             * Why should we convert the articleListJson into [String]?
             *
             * Well, [JSONObject] only accept [HashMap], [String], JSONTokener, and itself.
             * Since we're also going to take each Array element into the [Article] object,
             * we have to loop through the array to get each matching component.
             *
             * You can look how to get each key-value element
             * */
            val articleJson = JSONObject(articleListJson[i].toString())

            /** Get value based on specified key */
            val link = articleJson.getString("link")
            val title = articleJson.getString("title")
            val desc = articleJson.getString("desc")
            val date = articleJson.getString("date")
            val tags = articleJson.getString("tags")

            /**
             * Instantiate [Article] object and
             * pass each param from the value we've defined above
             * */
            val article = Article(link, title, desc, date, tags)

            /** Push the article into articleList */
            articleList.add(article)

//            Log.v(TAG, "Article $i = ${article}")

            i++
        }

        Log.v(TAG, "FirebaseDatabase successfully parsed")

        /** Last but not least, we return the articleList [MutableList] */
        return articleList
    } catch (e: Exception) {
        Log.wtf(TAG, "Failed to parse FirebaseData")
        e.printStackTrace()
        return mutableListOf()
    }
}