import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.words
import org.apache.commons.text.StringEscapeUtils
import java.io.File

val helpText = """
           Stackbot
A Discord bot for searching Stack Exchange sites

Commands

`${'$'}ping`
Check if server is alive

`${'$'}halp [query]`
Search a StackExchange site.   
The site searched depends on the current channel, and defaults to `stackoverflow`.

`${'$'}[subject name] [query]`
Search the StackExchange site corresponding to `[subject name]`

**Valid subject names:**
- `chem`
- `phys`
- `cs`
- `english`
- `math`""".trimIndent()

suspend fun main() {
  val token = File("secrets/token.txt").readText()
  val channels = loadChannels()
  bot(token) {
    commands("$") {
      command("help") {
        reply(helpText)
      }
      command("ping") {
        reply("pong")
      }

      command("halp") {
        val channel = channels.firstOrNull { it.id == this.channelId.toLong() }
        val queryWords = words.drop(1)
        val site = channel?.site ?: "stackoverflow"
        val query = queryWords.joinToString(" ")
        reply("", StackOverflow.discordSearch(query, site))
      }

      for (channel in channels) {
        command(channel.name) {
          val queryWords = words.drop(1)
          val site = channel.site
          val query = queryWords.joinToString(" ")
          reply("", StackOverflow.discordSearch(query, site))
        }
      }
    }
  }
}

fun String.unescapeHtml() = StringEscapeUtils.UNESCAPE_HTML4.translate(this)!!