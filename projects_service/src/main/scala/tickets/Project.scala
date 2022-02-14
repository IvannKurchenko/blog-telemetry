package tickets

import io.circe.Codec
import io.circe.generic.semiauto._

case class Project(id: Long,
                   name: String,
                   description: String,
                   users: List[String],
                   createdAt: Long,
                   createdBy: String,
                   modifiedAt: Long,
                   modifiedBy: String)

object Project {
  implicit val codec: Codec[Project] = deriveCodec
}