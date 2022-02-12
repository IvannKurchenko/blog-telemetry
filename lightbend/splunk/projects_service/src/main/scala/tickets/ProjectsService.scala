package tickets


import akka.stream.Materializer
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

import scala.concurrent.{ExecutionContext, Future}
import tickets.ObservableExtensions._

import java.util.UUID

class ProjectsService(mongo: MongoDatabase)
                     (implicit ec: ExecutionContext, materializer: Materializer){

  private lazy val collection = {
    mongo.getCollection[Project]("projects")
      .withCodecRegistry(fromRegistries(fromProviders(Project.codecProvider), DEFAULT_CODEC_REGISTRY))
  }

  def getProjects(): Future[List[Project]] = collection.find().all

  def createProject(createProject: CreateProject): Future[Project] = {
    val now = System.currentTimeMillis()

    val project = Project(
      id = UUID.randomUUID().toString,
      name = createProject.name,
      description = createProject.description,
      users = Nil,
      createdAt = now,
      createdBy = createProject.creator,
      modifiedAt = now,
      modifiedBy = createProject.creator
    )

    collection.insertOne(project).head.map(_ => project)
  }

  /*

  def deleteProject(id: Long): Future[Project] = {

  }

  def getProjectUsers(id: Long): Future[List[Long]] = {

  }

  def addProjectUser(id: Long, userId: Long): Future[List[Long]] = {

  }

  def deleteProjectUser(id: Long, userId: Long): Future[List[Long]] = {

  }*/
}


