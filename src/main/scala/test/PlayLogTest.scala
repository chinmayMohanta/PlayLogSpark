package test

import org.apache.spark.sql.SparkSession
import org.joda.time.DateTime
import java.io._

/**
  * Created by Chinmay on 30/04/2017.
  */
object PlayLogTest {
    def main(args: Array[String]) {

      // Simple user input validation
      if (args.length != 1) {
        System.err.println("Usage: provide a date : Format :: YYYY-MM-DD ")
        System.exit(1)
      }

      try {
        new DateTime(args(0))
      } catch {
        case e:Exception => { System.err.println("Correct date Format :: YYYY-MM-DD "); System.exit(1)}
      }

      val spark = SparkSession
        .builder().master("local")
        .appName("Play Log Test").master("local")
        .getOrCreate()

      // Read campaign from local fs
      // Remark : For the purpose of demonstation reading from local file system; but ideally it should be read from HDFS
      val csvRaw = spark.sparkContext.textFile("test/input/campaign.csv")
      // campaign without header
      val campaign = csvRaw.mapPartitionsWithIndex { (idx, iter) => if (idx == 0) iter.drop(1) else iter }.
        map(x => x.split(","))
     //campaign.foreach(x => println(x(0)))
      // Broad cast the campaign data to support map-side join
     val brdcastCampaign = spark.sparkContext.broadcast(campaign.collect())


      // Remark : For the purpose of demonstation reading from local file system; but ideally it should be read from HDFS
      // val logs = spark.sparkContext.textFile("hdfs://input/raw.json")
      //from local fs
      val logs = spark.sparkContext.textFile("test/input/raw_pop.json")


       //  TASK A

      // SubTask 1 : ENCODE DECODE  :: OPTMIZATION (cluster) :: mapPartitions
      //println("Original Play log")
      //val test = logs.map(x=> JsonPlayLogEncDec.decodePlyLogJson(x))
      // test.map(x =>JsonPlayLogEncDec.encodePlyLogJson(x)).foreach(y => println(y)) // Print for Test Only


      // SubTask 2 : Retain the logs (based on timestamp) only from a date supplied by the user
      val date = new DateTime(args(0));
      println("Play log only after   " + date)
      val datedLogs =logs.map(x=> JsonPlayLogEncDec.decodePlyLogJson(x))
        .filter(y => y.body.timestamp.isAfter(date))
      //datedLogs.foreach(y => println(y))  // Print for Test Only


      // SubTask 3: Enrich the playLog with fake campaignid & campaignname if missing
      val enreachedlogsWithFakeData = datedLogs.mapPartitions(iter => {val playLogs = iter.toList;
        playLogs.map(x => if (x.body.campaignid == null && x.body.campaignname ==null)
        {val newbody = x.body.copy(campaignid=Some("fakeCampaignID"),campaignname=Some("fakeCampaignName"));
          val updPlaylog=x.copy(body=newbody); updPlaylog} else x ).iterator})
      enreachedlogsWithFakeData.map(x =>JsonPlayLogEncDec.encodePlyLogJson(x,part=2)).foreach(y => println(y))  // Print for Test Only




      // SubTask 4: Enrich the playLog with fake campaignid & campaignname from CSV  if missing  :: map-Side join
      // Which campaignid/name to pick from the CSV ?? Assuming the first ccampaignid/name to be picked

      val enreachedlogs = datedLogs.mapPartitions(iter => {val playLogs = iter.toList;
        playLogs.map(x => if (x.body.campaignid == null && x.body.campaignname ==null)
        {val newbody = x.body.copy(campaignid=Some(brdcastCampaign.value(0)(0)),campaignname=Some(brdcastCampaign.value(0)(1)));
          val updPlaylog=x.copy(body=newbody); updPlaylog} else x ).iterator})

      // For the purpose of demonstration only local FS writes are supported
      // In the clustered environment it will be HDFS
      // Only for local writes
      PrintFile.setWritter("test/result/enriched_pop.json")
      enreachedlogs.map(x =>JsonPlayLogEncDec.encodePlyLogJson(x,part=2)).foreach(y => PrintFile.printLine(y))  // Print for Test Only
      PrintFile.closeWritter()


      //  TASK B
      // Aggregation : with Time,Space dimensions

      val aggrDuration=enreachedlogs.map(x => (x.body.playerid+"#"+x.body.creativeid+"#"+
        x.body.timestamp.year().get().toString+"#"+x.body.timestamp.monthOfYear().get().toString+"#"+
        x.body.timestamp.dayOfMonth().get().toString+"#"+x.body.timestamp.hourOfDay().
        get().toString,{if (x.body.duration !=null) x.body.duration.get else 0 })).reduceByKey(_+_)



      val durationjson = aggrDuration.map(x => {val tkns= x._1.split("#"); "{"+ "\"playerid\":"+"\""+
        tkns(0)+"\""+","+"\"creativeid\":"+"\""+tkns(1)+"\""+","+"\"hour\":"+tkns(2)+tkns(3)+
        tkns(4)+tkns(5)+","+"\"duration\":"+x._2+"}"})

      // For the purpose of demonstration only local FS writes are supported
      // In the clustered environment it will be HDFS
      PrintFile.setWritter("test/result/aggregate_pop.json")
      durationjson.foreach(y => PrintFile.printLine(y))
      PrintFile.closeWritter()

      spark.stop()
    }


}

object PrintFile{
  val writter:Array[PrintWriter] = new Array[PrintWriter](1)

  def setWritter(path:String) ={
    val file:File =  new File(path)
    if (file.exists()) {file.delete(); file.createNewFile(); writter(0)=new PrintWriter(file)} else writter(0)=new PrintWriter(file)

  }

  def printLine(str:String): Unit ={
    writter(0).write(str)
    writter(0).write("\n")

  }

  def closeWritter() = {
    writter(0).close()
  }

}
