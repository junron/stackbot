import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

data class DiscordChannel(val name: String, val site: String, val id: Long)

fun loadChannels(): List<DiscordChannel> {
  val listOfChannelsType = object : TypeToken<ArrayList<DiscordChannel>>() {}.type
  return Gson().fromJson(getResourceAsText("/channel-mappings.json"), listOfChannelsType)
}