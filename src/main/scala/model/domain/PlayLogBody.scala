package model.domain

import org.joda.time.DateTime

case class PlayLogBody(
                        playerid: String,
                        hostname: Option[String],
                        playertype: String,
                        timestamp: DateTime,
                        campaignid: Option[String],
                        campaignname: Option[String],
                        duration: Option[Int],
                        creativeid: String,
                        frameid: Option[String],
                        status: Option[PlayLogStatus],
                        media: Seq[PlayLogMedia],
                        condition: Seq[PlayLogCondition],
                        context: Seq[PlayLogContext]
                      )
