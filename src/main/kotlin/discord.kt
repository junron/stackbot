import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

data class DiscordChannel(val name: String, val site: String, val id: Long)

fun loadChannels(): List<DiscordChannel> {
  val listOfChannelsType = object : TypeToken<ArrayList<DiscordChannel>>() {}.type
  return Gson().fromJson(File("secrets/channel-mappings.json").readText(), listOfChannelsType)
}