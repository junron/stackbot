import org.apache.commons.text.StringEscapeUtils

fun String.parseTags(): String {
  return this.replace(Regex("\\[.+]")) {
    it.value.replace(" ", "-")
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

fun String.hasUnmatchedBackticks() = this.split("```").size % 2 == 0
fun String.unescapeHtml() = StringEscapeUtils.UNESCAPE_HTML4.translate(this)!!