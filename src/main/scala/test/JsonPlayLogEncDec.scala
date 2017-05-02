package test

import model.domain._
import org.joda.time.DateTime

/**
  * Created by Chinmay on 29/04/2017.
  */

// JSON : String to Object
object JsonPlayLogEncDec {

  // PlayLog encoder
  def  decodePlyLogJson(plylogRaw:String):PlayLog = {
    scala.util.parsing.json.JSON.parseFull(plylogRaw) match {
      case Some(x:Map[String,Map[String,Any]]) =>
        new PlayLog (new PlayLogHeader(
          java.lang.Double.parseDouble(x("header") ("version").toString).toInt,
          x("header") ("makerId").toString,
          if (x("header").contains("envId")) Some(java.lang.Double.parseDouble(x("header") ("envId").toString).toInt) else null,
          x("header") ("id").toString,
          DateTime.parse(x("header") ("creationTime").toString)
        ),new PlayLogBody (
          x("body") ("playerid").toString,
          Some(x("body") ("hostname").toString),
          x("body") ("playertype").toString,
          DateTime.parse(x("body") ("timestamp").toString),
          if (x("body").contains("campaignid")) Some(x("body") ("campaignid").toString) else null,
          if (x("body").contains("campaignname")) Some(x("body") ("campaignname").toString) else null,
          if (x("body").contains("duration")) Some(java.lang.Double.parseDouble(x("header") ("version").toString).toInt) else null,
          x("body") ("creativeid").toString,
          if (x("body").contains("frameid"))  Some(x("body") ("frameid").toString) else null,
          if (x("body").contains("status"))  {if (x("body").contains("status").toString.equalsIgnoreCase("OK")) Some(PlayLogStatus.OK) else Some(PlayLogStatus.KO)} else null,
          plyLogMediaHelper(x("body")("media")),
          plyLogConditionHelper(x("body")("condition")),
          plyLogContextHelper(x("body")("context"))

        ))
    }


  }

  def plyLogMediaHelper(logMedias:Any): Seq[PlayLogMedia] = {

    logMedias match {
      case v:List[Map[String,Any]] => v.map(m => new PlayLogMedia(m("filename").toString,if (m.contains("hash")) Some(m("hash").toString) else null,if (m.contains("timestamp")) Some(m("timestamp").toString) else null))
    }

  }

  def plyLogConditionHelper(logCondition:Any): Seq[PlayLogCondition] = {

    logCondition match {
      case v:List[Map[String,Any]] => v.map(m => new PlayLogCondition(m("name").toString,m("value").toString,if (m.contains("origin")) Some(m("origin").toString) else null))
    }

  }

  def plyLogContextHelper(logContext:Any): Seq[PlayLogContext] = {

    logContext match {
      case v:List[Map[String,Any]] => v.map(m => new PlayLogContext(m("key").toString,m("value").toString))
    }

  }

  /*
  @part : 1 = header, 2 = body, 3 (default) = header + body
   */
  // JSON : Object to String
  def  encodePlyLogJson(playLlogobj:PlayLog, part:Int=3):String = {
    /*
    {"header":{"version":2,"makerId":"LeChat-PPR","id":"d7d35fa9-0461-4d6d-bc76-e7a879e1f887","creationTime":"2017-03-29T13:54:04+0200"},
    "body":{"playerid":"35776754","hostname":"fr-lab-lx0014.jcd.priv","playertype":"dms-player",
    "timestamp":"2017-03-29T15:46:59+0200","duration":10,"creativeid":"102675169","frameid":"50732058","media":[],"condition":[],"context":[]}}
     */

    val header = "\"header\"" + ":" + "{" + "\"version\"" + ":"+  playLlogobj.header.version + "," +
      "\"makerId\""+ ":"  +"\"" + playLlogobj.header.makerId +"\"" + "," + "\"id\"" + ":" + "\"" +playLlogobj.header.id+"\"" + "," +
      {playLlogobj.header.envId match {case x:Some[Int] => "\"envId\""+ ":" + x.get + "," ; case _ => ""}}+
      "\"creationTime\"" + ":" +"\"" + playLlogobj.header.creationTime +"\"" + "}"
    //println(header)

    val body = "\"body\"" + ":" + "{" + "\"playerid\"" + ":" +"\"" +playLlogobj.body.playerid + "\"" +"," +
      {playLlogobj.body.hostname match {case x:Some[String] => "\"hostname\""+ ":"+ "\"" + x.get+"\""  + "," ; case _ => ""}}+
      "\"playertype\"" + ":" +"\"" +playLlogobj.body.playertype + "\"" +"," +
      "\"timestamp\"" + ":" +"\"" +playLlogobj.body.timestamp +"\"" + "," +
      {playLlogobj.body.campaignid match {case x:Some[String] => "\"campaignid\""+ ":" +"\""  + x.get +"\"" + "," ; case _ => ""}}+
      {playLlogobj.body.campaignname match {case x:Some[String] => "\"campaignname\""+ ":" +"\""  + x.get +"\"" + "," ; case _ => ""}}+
      {playLlogobj.body.duration match {case x:Some[Int] => "\"duration\""+ ":" + x.get + "," ; case _ => ""}}+
      "\"creativeid\"" + ":" +"\"" +playLlogobj.body.creativeid +"\"" + "," +
      {playLlogobj.body.frameid match {case x:Some[String] => "\"frameid\""+ ":" +"\"" + x.get +"\"" + "," ; case _ => ""}}+
      {playLlogobj.body.status match {case x:Some[PlayLogStatus] => "\"status\""+ ":" +"\"" + x.get +"\"" + "," ; case _ => ""}}+
      "\"media\"" + ":" +plyLogMediaDecHelper(playLlogobj.body.media) + "," +
      "\"condition\"" + ":" +plyLogConditionDecHelper(playLlogobj.body.condition) + ","+
      "\"context\"" + ":" +plyLogContextDecHelper(playLlogobj.body.context) +
     "}"


    if (part == 1) "{"+header+"}" else if (part == 2) "{"+body+"}" else "{" + header + "," + body +"}"

  }

  def plyLogMediaDecHelper(logMedias:Seq[PlayLogMedia]):String = {

    val logmedia = logMedias match {
      case v:Seq[PlayLogMedia] => v.map(x => "{" + "\"filename\"" + ":" + "\""  +x.filename +"\"" + "," +
        {x.hash match {case x:Some[String] => "\"hash\""+ ":"+"\""  + x.get +"\"" + "," ; case _ => ""}}+
        {x.timestamp match {case x:Some[String] => "\"timestamp\""+ ":" +"\""  + x.get +"\""   ; case _ => ""}}+"}").mkString(",")

          }
    "[" + logmedia + "]"

  }

  def plyLogConditionDecHelper(logCond:Seq[PlayLogCondition]):String = {

    val logcond = logCond match {
      case v:Seq[PlayLogCondition] => v.map(x => "{" + "\"filename\"" + ":" +"\"" +x.name +"\"" + "," +
        "\"value\"" + ":"+"\""  +x.value +"\"" + ","+
        {x.origin match {case x:Some[String] => "\"origin\""+ ":"+"\""  + x.get +"\""  ; case _ => ""}}+"}").mkString(",")

    }
    "[" + logcond + "]"

  }

  def plyLogContextDecHelper(logContext:Seq[PlayLogContext]):String = {

    val logcontext = logContext match {
      case v:Seq[PlayLogContext] => v.map(x => "{" + "\"key\"" + ":"+"\""  +x.key +"\"" + "," +
        "\"value\"" + ":"+"\""  +x.value +"\"" + "}").mkString(",")

    }
    "[" + logcontext + "]"

  }

  /*
  // stub for unit testing
  def main(args:Array[String]){

    val playLog:String = "{\"header\":{\"version\":2,\"makerId\":\"LeChat-PPR\",\"id\":\"d7d35fa9-0461-4d6d-bc76-e7a879e1f887\",\"envId\":12,\"creationTime\":\"2017-03-29T13:54:04+0200\"},\"body\":{\"playerid\":\"35776754\",\"hostname\":\"fr-lab-lx0014.jcd.priv\",\"playertype\":\"dms-player\",\"timestamp\":\"2017-03-29T15:46:59+0200\",\"duration\":10,\"creativeid\":\"102675169\",\"frameid\":\"50732058\",\"media\":[],\"condition\":[],\"context\":[]}}"
    val playLog1:String = "{\"header\":{\"version\":2,\"makerId\":\"LeChat-PPR\",\"id\":\"d7d35fa9-0461-4d6d-bc76-e7a879e1f887\",\"creationTime\":\"2017-03-29T13:54:04+0200\"},\"body\":{\"playerid\":\"35776754\",\"hostname\":\"fr-lab-lx0014.jcd.priv\",\"playertype\":\"dms-player\",\"timestamp\":\"2017-03-29T15:46:59+0200\",\"duration\":10,\"creativeid\":\"102675169\",\"frameid\":\"50732058\",\"media\":[{\"filename\":\"file1\",\"hash\":\"dumy\",\"timestamp\":\"2017-04-03T16:59:53+0200\"},{\"filename\":\"file2\",\"hash\":\"dumy2\",\"timestamp\": \"2017-04-03T16:59:53+0200\"}],\"condition\":[],\"context\":[]}}"
    val playLogfull:String = "{\"header\":{\"version\":2,\"makerId\":\"LeChat-PPR\",\"id\":\"d7d35fa9-0461-4d6d-bc76-e7a879e1f887\",\"creationTime\":\"2017-03-29T13:54:04+0200\"},\"body\":{\"playerid\":\"35776754\",\"hostname\":\"fr-lab-lx0014.jcd.priv\",\"playertype\":\"dms-player\",\"timestamp\":\"2017-03-29T15:46:59+0200\",\"duration\":10,\"creativeid\":\"102675169\",\"frameid\":\"50732058\",\"media\":[{\"filename\":\"file1\",\"hash\":\"dumy\",\"timestamp\":\"2017-04-03T16:59:53+0200\"},{\"filename\":\"file2\",\"hash\":\"dumy2\",\"timestamp\": \"2017-04-03T16:59:53+0200\"}],\"condition\":[{\"name\":\"name1\",\"value\":\"value1\",\"origin\": \"origin1\"},{\"name\":\"name2\",\"value\":\"value2\",\"origin\": \"origin2\"}],\"context\":[{\"key\":\"key1\",\"value\":\"value1\"},{\"key\":\"key2\",\"value\":\"value2\"}]}}"

    val test:String = "{\"header\":{\"version\":2,\"makerId\":\"LeChat-PPR\",\"id\":\"d7d35fa9-0461-4d6d-bc76-e7a879e1f887\",\"creationTime\":\"2017-03-29T13:54:04+0200\"},\"body\":{\"playerid\":\"35776754\",\"hostname\":\"fr-lab-lx0014.jcd.priv\",\"playertype\":\"dms-player\",\"timestamp\":\"2017-03-29T15:46:59+0200\",\"duration\":10,\"creativeid\":\"102675169\",\"frameid\":\"50732058\",\"media\":[],\"condition\":[],\"context\":[]}}"
    //val plg = scala.util.parsing.json.JSON.parseFull(playLog)
    //val plg1 = scala.util.parsing.json.JSON.parseFull(playLog1)
    //println(plg1)
    //println(decodePlyLogJson(playLog))
    //println(decodePlyLogJson(playLog1))


     // Decoding

    println(encodePlyLogJson(decodePlyLogJson(test),3))

  }
  */

}
