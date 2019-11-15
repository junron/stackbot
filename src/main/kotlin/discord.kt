import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

data class DiscordChannel(val name: String, val site: String, val id: Long)

fun loadChannels(): List<DiscordChannel> {
  val listOfChannelsType = object : TypeToken<ArrayList<DiscordChannel>>() {}.type
  val channels: List<DiscordChannel> =
    Gson().fromJson(File("secrets/channel-mappings.json").readText(), listOfChannelsType)
  return channels + listOf(DiscordChannel("halp", "stackoverflow", 0L))
}

object EmojiMappings {
  const val trash = "\uD83D\uDDD1️"
  const val arrowRight = "➡️"
  const val arrowLeft = "⬅️"
}