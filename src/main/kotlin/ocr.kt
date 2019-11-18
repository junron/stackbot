import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder
import com.amazonaws.services.rekognition.model.DetectTextRequest
import com.amazonaws.services.rekognition.model.Image
import java.io.File
import java.nio.ByteBuffer

object OCR {
  private val credsRaw = File("secrets/aws-creds").readLines().map { it.trim() }
  private val credentials = BasicAWSCredentials(credsRaw[0], credsRaw[1])
  private val client = AmazonRekognitionClientBuilder.standard()
    .withCredentials(AWSStaticCredentialsProvider(credentials))
    .withRegion("ap-southeast-1")
    .build()

  private var counter = 0

  fun detect(bytes: ByteArray): String {
    counter++
    if (counter > 50) return "Rate limited exceeded"
    val request = DetectTextRequest().withImage(
      Image().withBytes(
        ByteBuffer.wrap(bytes)
      )
    )

    val result = client.detectText(request).textDetections
    return result.fold("") { acc, text ->
      if (text.type == "LINE") {
        acc + text.detectedText + "\n"
      } else acc
    }
  }
}