import io.ktor.client.HttpClient
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.mine.HttpCookies
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.Parameters
import org.jsoup.Jsoup
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

object Espace {
  private val httpClient = HttpClient {
    install(HttpCookies) {
      // Will keep an in-memory map with all the cookies from previous requests.
      storage = AcceptAllCookiesStorage()
    }
    followRedirects = false
  }
  private const val baseUrl = "https://espace.nushigh.edu.sg/"
  private val credentials = File("secrets/espace").readLines().map { it.trim() }

  suspend fun getData(): MutableList<MentorGroup> {
    val results = mutableListOf<MentorGroup>()
    while (!isSignedIn()) signIn()
    val personalData = httpClient.get<String>(baseUrl + "SDS/Student/Student_Profile.aspx")
    val document = Jsoup.parse(personalData)
    document.select("#StudentDetails_table_Details tr:not(.DataGridCtrl-Header)")
      .forEach {
        val year = it.select("td[align=center]").first().text()
        if (year == "2020") {
          val elements = it.select("td").toList().drop(3)
          val mg = MentorGroup(
            elements.first().text(),
            elements.component2().text(),
            elements.component3().text()
          )
          results += mg
        }
      }
    return results
  }

  private suspend fun signIn() {
    val (username, password) = credentials
    val response = httpClient.get<String>(baseUrl + "Lms/default.aspx")
    val parameters = mutableMapOf<String, String>()
    Jsoup.parse(response).select("input[type=hidden]").forEach {
      parameters[it.attr("name")] = it.attr("value")
    }
    parameters["__EVENTTARGET"] = ""
    parameters["__EVENTARGUMENT"] = ""
    parameters["__LASTFOCUS"] = ""
    parameters["ctl00${'$'}domain"] = "eSPACE"
    parameters["ctl00${'$'}userid"] = username
    parameters["ctl00${'$'}password"] = password
    httpClient.post<HttpResponse>(baseUrl + "Lms/default.aspx") {
      body = FormDataContent(Parameters.build {
        parameters.forEach { (key, value) -> append(key, value) }
      })
    }
    println("Logged in at ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}")
  }

  private suspend fun isSignedIn() =
    httpClient.get<HttpResponse>(baseUrl + "SDS/Student/Student_Profile.aspx")
      .status.value == 200
}

data class MentorGroup(
  val name: String,
  val mentor: String,
  val yearHead: String
)
