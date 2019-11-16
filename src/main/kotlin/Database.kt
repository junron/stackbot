import com.toddway.shelf.FileStorage
import com.toddway.shelf.KotlinxSerializer
import com.toddway.shelf.Shelf
import com.toddway.shelf.get
import java.io.File

object Database {
  private val database = Shelf(FileStorage(File("secrets/data")), KotlinxSerializer().apply {
    register(Query.serializer())
  })

  operator fun get(id: String) =
    database.item(id).get<Query>()

  operator fun minusAssign(id: String) {
    database.item(id).remove()
  }

  operator fun set(id: String, query: Query) {
    database.item(id).put(query)
  }
}