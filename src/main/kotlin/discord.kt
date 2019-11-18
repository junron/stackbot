import com.github.kittinunf.fuel.httpGet
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

fun downloadFile(url: String): ByteArray? {
  val response = url.httpGet()
    .header("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
    .response().third
  return response.component1()
}

object EmojiMappings {
  const val trash = "\uD83D\uDDD1"
  const val arrowRight = "▶"
  const val arrowLeft = "◀"
  const val eyes = "\uD83D\uDC40"
}