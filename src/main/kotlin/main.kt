import com.jessecorbett.diskord.api.model.stringified
import com.jessecorbett.diskord.api.rest.MessageEdit
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.words
import java.io.File

val helpText = """
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
  val token = File("secrets/token.txt").readText().trim()
  val messages = mutableMapOf<Long, Query>()
  val channels = loadChannels()
  bot(token) {
    reactionAdded {
      //      Return if message is not sent by bot
      val query = messages[it.messageId.toLong()] ?: return@reactionAdded
      val message = clientStore.channels[it.channelId].getMessage(it.messageId)
      val reaction = message.reactions.firstOrNull { reaction -> reaction.emoji == it.emoji } ?: return@reactionAdded
      if (reaction.count == 1) return@reactionAdded
      when (reaction.emoji.stringified) {
        EmojiMappings.trash -> {
          message.delete()
        }
        EmojiMappings.arrowRight -> {
          if (query.increment()) {
            clientStore.channels[it.channelId].editMessage(
              it.messageId, MessageEdit(
                "",
                embed = query.cache[query.answerNumber]
              )
            )
          }
        }
        EmojiMappings.arrowLeft -> {
          if (query.decrement()) {
            clientStore.channels[it.channelId].editMessage(
              it.messageId, MessageEdit(
                "",
                embed = query.cache[query.answerNumber]
              )
            )
          }
        }
      }

    }
    commands("$") {
      command("help") {
        reply(helpText)
      }
      command("ping") {
        reply("pong")
      }

      for (channel in channels) {
        command(channel.name) {
          println("author: ${author.username}")
          var site = channel.site
          if (channel.id == 0L) {
            val realChannel = channels.firstOrNull { it.id == this.channelId.toLong() }
            site = realChannel?.site ?: "stackoverflow"
          }
          val queryWords = words.drop(1)
          val queryString = queryWords.joinToString(" ")
          val query = Query(queryString, site)
          val message = reply("", StackOverflow.discordSearch(query))
          if (query.answerNumber == -1) return@command
          messages[message.id.toLong()] = query
          message.react(EmojiMappings.arrowLeft)
          message.react(EmojiMappings.trash)
          message.react(EmojiMappings.arrowRight)
        }
      }
    }
  }
}