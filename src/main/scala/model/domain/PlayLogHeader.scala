package model.domain

import org.joda.time.DateTime

//auto generated code by avro4s
case class PlayLogHeader(
  version: Int,
  makerId: String,
  envId: Option[Int],
  id: String,
  creationTime: DateTime
)
