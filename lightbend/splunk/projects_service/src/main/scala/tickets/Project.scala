package tickets

import io.circe.Codec
import io.circe.generic.semiauto._
import org.mongodb.scala.bson.codecs.Macros

case class Project(id: String,
                   name: String,
                   description: String,
                   users: List[String],
                   createdAt: Long,
                   createdBy: String,
                   modifiedAt: Long,
                   modifiedBy: String)

object Project {
  implicit val codec: Codec[Project] = deriveCodec
  val codecProvider = Macros.createCodecProvider[Project]()
}

case class CreateProject(name: String,
                         description: String,
                         users: List[String],
                         creator: String)

object CreateProject {
  implicit val codec: Codec[CreateProject] = deriveCodec
}
