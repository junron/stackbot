import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.jessecorbett.diskord.api.rest.Embed
import com.jessecorbett.diskord.dsl.embed
import com.jessecorbett.diskord.dsl.footer
import com.jessecorbett.diskord.util.Colors
import kotlinx.serialization.Serializable

class StackOverflow {
  companion object StackOverflow {
    private const val baseUrl = "https://api.stackexchange.com/2.2/search/advanced"
    private fun search(query: String, site: String): String {
      return baseUrl.httpGet(
        listOf(
          "q" to query,
          "sort" to "relevance",
          "order" to "desc",
          "site" to site,
          //  Only include answers
          "filter" to "!*1SgQGDMkNpCIzzMCq25IRX4u0u-1D8S2YxUITK_Q",
          "answers" to 1,
          "pagesize" to 5
        )
      ).responseString().component3().component1()!!
    }

    fun discordSearch(query: Query): Embed {
      println(query)
      val (queryString, site) = query
      if (queryString.isBlank()) {
        return embed {
          title = "No results: empty query"
          color = Colors.RED
        }
      }
      val response =
        Gson().fromJson(search(queryString.parseTags(), site), StackResponse::class.java)
      if (response.items.isEmpty()) {
        return embed {
          title = "No results for $queryString"
          color = Colors.RED
        }
      }
      val embeds: List<Embed> = response.items.mapIndexed { index, item ->
        var description = (item.body_markdown +
            "\n\n**Answer:**\n" +
            item.answers[0].body_markdown).unescapeHtml().parseAsCode()
        //    Truncate response if too long
        if (description.length > 2048) {
          description = description.substring(0, 2045) + "..."
          if (description.hasUnmatchedBackticks()) description = description.substring(0, 2042) + "```..."
        }
        embed {
          this.description = description
          title = item.title.unescapeHtml()
          url = item.link
          color = Colors.GREEN
          footer("Answer $index/${response.items.size - 1}")
        }
      }
      query.cache = embeds
      query.increment()
      return embeds[0]
    }
  }
}

data class StackAnswers(val body_markdown: String)
data class StackData(val answers: List<StackAnswers>, val body_markdown: String, val title: String, val link: String)
data class StackResponse(val items: List<StackData>)

@Serializable
data class Query(val query: String, val site: String, val userId: String) {
  var answerNumber = -1
    private set

  lateinit var cache: List<Embed>
  fun increment(): Boolean {
    answerNumber++
    if (answerNumber >= cache.size) {
      answerNumber--
      return false
    }
    return true
  }

  fun decrement(): Boolean {
    answerNumber--
    if (answerNumber < 0) {
      answerNumber++
      return false
    }
    return true
  }
}
