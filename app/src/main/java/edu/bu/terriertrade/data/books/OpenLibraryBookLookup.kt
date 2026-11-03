package edu.bu.terriertrade.data.books

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Looks books up on Open Library (free, no API key):
 * https://openlibrary.org/api/books\?bibkeys\=ISBN:\<isbn\>\&format\=json\&jscmd\=data
 * The reply is read with Android's built-in org.json, so no extra libraries are needed.
 */
class OpenLibraryBookLookup(
    private val api: OpenLibraryApi = createApi(),
) : BookLookup {

    override suspend fun findByIsbn(isbn: String): Result<BookInfo> =
        try {
            val body = api.books(bibKeys = "ISBN:$isbn").string()
            val book = withContext(Dispatchers.Default) { parse(isbn, body) }
            if (book != null) Result.success(book) else Result.failure(BookNotFound(isbn))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.w("OpenLibrary", "Lookup failed for $isbn", e)
            Result.failure(Exception("Couldn't reach Open Library. Check your connection and try again."))
        }
    private fun parse(isbn: String, json: String): BookInfo? {
        val book = JSONObject(json).optJSONObject("ISBN:$isbn") ?: return null
        val title = listOf(book.optString("title"), book.optString("subtitle"))
            .filter { it.isNotBlank() }
            .joinToString(": ")
            .ifBlank { return null }
        val authorsJson = book.optJSONArray("authors")
        val authors = (0 until (authorsJson?.length() ?: 0)).mapNotNull { i ->
            authorsJson?.optJSONObject(i)?.optString("name")?.takeIf { it.isNotBlank() }
        }
        val year = Regex("\\d{4}").find(book.optString("publish_date"))?.value
        val cover = book.optJSONObject("cover")?.let { it.optString("medium").ifBlank { it.optString("large") } }
        return BookInfo(
            isbn = isbn,
            title = title,
            authors = authors,
            publishYear = year,
            coverUrl = cover?.takeIf { it.isNotBlank() },
        )
    }

    interface OpenLibraryApi {
        @GET("api/books")
        suspend fun books(
            @Query("bibkeys") bibKeys: String,
            @Query("format") format: String = "json",
            @Query("jscmd") jscmd: String = "data",
        ): ResponseBody
    }

    companion object {
        private const val BASE_URL = "https://openlibrary.org/"

        fun createApi(): OpenLibraryApi {
            // Open Library asks apps to say who they are.
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().header("User-Agent", "TerrierTrade/1.0 (BU student project)").build())
                }
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .build()
                .create(OpenLibraryApi::class.java)
        }
    }
}
