import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.words
import org.apache.commons.text.StringEscapeUtils


suspend fun main() {
  val token = getResourceAsText("/token.txt")
  val channels = loadChannels()
  bot(token) {
    commands("$") {
      command("ping") {
        reply("pong")
      }

      command("halp") {
        val channel = channels.firstOrNull { it.id == this.channelId.toLong() }
        val queryWords = words.drop(1)
        val site = channel?.site ?: "stackoverflow"
        val query = queryWords.joinToString(" ")
        reply("",StackOverflow.discordSearch(query,site))
      }

      for (channel in channels) {
        command(channel.name) {
          val queryWords = words.drop(1)
          val site = channel.site
          val query = queryWords.joinToString(" ")
          reply("",StackOverflow.discordSearch(query,site))
        }
      }
    }
  }
}

fun getResourceAsText(path: String): String {
  return object {}.javaClass.getResource(path).readText()
}

fun String.unescapeHtml() = StringEscapeUtils.UNESCAPE_HTML4.translate(this)!!