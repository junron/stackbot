import com.jessecorbett.diskord.api.exception.DiscordBadPermissionsException
import com.jessecorbett.diskord.api.model.stringified
import com.jessecorbett.diskord.api.rest.MessageEdit
import com.jessecorbett.diskord.dsl.Bot
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.authorId
import com.jessecorbett.diskord.util.sendMessage
import com.jessecorbett.diskord.util.words
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*

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

lateinit var botInstance: Bot
suspend fun main() {
  val token = File("secrets/token.txt").readText().trim()
  mutableMapOf<Long, Query>()
  val channels = loadChannels()
  GlobalScope.launch {
    bot(token) {
      botInstance = this
      reactionAdded {
        val message = clientStore.channels[it.channelId].getMessage(it.messageId)
        if (it.emoji.stringified == EmojiMappings.eyes) {
          val attachment = message.attachments.first()
//        Return if not image
          attachment.imageHeight ?: return@reactionAdded
//         Return if image size > 1MB
          if (attachment.sizeInBytes > 1e6) return@reactionAdded
          clientStore.channels[it.channelId].sendMessage(
            OCR.detect(
              downloadFile(attachment.url) ?: return@reactionAdded
            )
          )
          return@reactionAdded
        }
        //      Return if message is not sent by bot
        val query = Database[it.messageId] ?: return@reactionAdded
        val reaction =
          message.reactions.firstOrNull { reaction -> reaction.emoji == it.emoji } ?: return@reactionAdded
        if (reaction.count == 1) return@reactionAdded
        when (reaction.emoji.stringified) {
          EmojiMappings.trash -> {
//          Prevent unauthorized deletion
            if (query.userId != it.userId) return@reactionAdded
            Database -= it.messageId
            message.delete()
            return@reactionAdded
          }
          EmojiMappings.arrowRight -> {
            if (query.increment()) {
              clientStore.channels[it.channelId].editMessage(
                it.messageId, MessageEdit(
                  "",
                  embed = query.cache[query.answerNumber]
                )
              )
              Database[it.messageId] = query
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
              Database[it.messageId] = query
            }
          }
          else -> return@reactionAdded
        }
        try {
          clientStore.channels[it.channelId].removeMessageReaction(
            it.messageId,
            reaction.emoji.stringified,
            it.userId
          )
        } catch (e: DiscordBadPermissionsException) {
          println("Bad permissions for channel: ${it.channelId}")
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
            val query = Query(queryString, site, authorId)
            val message = reply("", StackOverflow.discordSearch(query))
            if (query.answerNumber == -1) return@command
            Database[message.id] = query
            message.react(EmojiMappings.arrowLeft)
            message.react(EmojiMappings.trash)
            message.react(EmojiMappings.arrowRight)
          }
        }
      }
    }
  }
  startChecking()
}

fun startChecking() {
  val timer = Timer()
  val task = object : TimerTask() {
    override fun run() {
      if (::botInstance.isInitialized) {
        runBlocking {
          val data = Espace.getData()
          if (data.size > 0) {
            botInstance.clientStore.channels["643368064448200705"].sendMessage(data.first().toString())
            cancel()
          }
        }
      } else {
        println("Waiting")
      }
    }
  }
  timer.schedule(task, 0, 60000)
}
