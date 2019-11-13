import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.jessecorbett.diskord.api.rest.Embed
import com.jessecorbett.diskord.dsl.embed
import com.jessecorbett.diskord.util.Colors

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
//        Only include answers
          "filter" to "!*1SgQGDMkNpCIzzMCq25IRX4u0u-1D8S2YxUITK_Q",
          "answers" to 1,
          "pagesize" to 1
        )
      ).responseString().component3().component1()!!
    }

    fun discordSearch(query: String, site: String): Embed {
      println(query)
      if (query.isBlank()) {
        return embed {
          title = "No results for $query"
          color = Colors.RED
        }
      }
      val response = Gson().fromJson(search(query, site), StackResponse::class.java)
      if (response.items.isEmpty()) {
        return embed {
          title = "No results for $query"
          color = Colors.RED
        }
      }
      val item = response.items[0]
      var description = (item.body_markdown +
          "\n\n**Answer:**\n" +
          item.answers[0].body_markdown).unescapeHtml().parseAsCode()
//    Truncate response if too long
      if (description.length > 2048) {
        description = description.substring(0, 2045) + "..."
        if (hasUnmatchedBackticks(description)) description = description.substring(0, 2042) + "```..."
      }
      return embed {
        this.description = description
        title = item.title.unescapeHtml()
        url = item.link
        color = Colors.GREEN
      }
    }
  }
}

fun String.parseAsCode(): String {
  var result = ""
  var isInCode = false
  var language = ""
  val lines = this.lines()
  for (line in lines) {
//      Stackoverflow 4 space code formatting
    val startsWith4Space = line.startsWith(" ".repeat(4)) && line.isNotBlank()
    val snippetStart = line.startsWith("<!-- language: ")
    val snippetEnd = line == "<!-- end snippet -->"
    if (snippetStart) {
      language = line.removePrefix("<!-- language: ").removePrefix("lang-").removeSuffix(" -->") + "\n"
      continue
    }
    if (snippetEnd) {
      language = ""
    }
    if (line.startsWith("<!--")) continue
    if (startsWith4Space xor isInCode) result += "```"
    if (startsWith4Space xor isInCode && !isInCode) result += language
    isInCode = startsWith4Space
//    Remove stackoverflow indent
    result += if (isInCode) {
      line.removePrefix(" ".repeat(4))
    } else {
      line
    }
    result += "\n"
  }
//  Condense extra newlines
  result = result.replace("\n\n", "\n")
  return result
}

fun hasUnmatchedBackticks(text: String) = text.split("```").size % 2 == 0

data class StackAnswers(val body_markdown: String)
data class StackData(val answers: List<StackAnswers>, val body_markdown: String, val title: String, val link: String)
data class StackResponse(val items: List<StackData>)
