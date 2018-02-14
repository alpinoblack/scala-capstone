package observatory

import java.nio.file.Paths
import java.time.LocalDate

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

/**
  * 1st milestone: data extraction
  */
object Extraction {

  val sparkSession: SparkSession =
    SparkSession
      .builder()
      .appName("Time Usage")
      .config("spark.master", "local")
      .getOrCreate

  import sparkSession.implicits._

  case class Station(stationId: String, location: Location)
  case class DailyTemperature(stationId: String, month: Short, day: Short, temperatureF: Temperature)


  def fsPath(resource: String): String =
    Paths.get(getClass.getResource(resource).toURI).toString

  val stationsSchema = StructType(
    List(
      StructField("stn",StringType,true),
      StructField("wban", StringType, true),
      StructField("latitude",StringType, true),
      StructField("longitude",StringType, true)
    )
  )

  val tempSchema = StructType(
    List(
      StructField("stn",StringType,true),
      StructField("wban", StringType, true),
      StructField("month",StringType, true),
      StructField("day",StringType, true),
      StructField("temperatureF",StringType, true)
    )
  )

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    val stationsDFraw = sparkSession
      .sqlContext
      .read
      .format("com.databricks.spark.csv")
      .schema(stationsSchema)
      .option("header", false)
      .load(fsPath(stationsFile))

    //TODO see if filter can be replaced by drop
    //TODO using custom function in order to filter values which cannot be cast as Numerics
    val stationsDS = stationsDFraw
     // .filter($"stn".isNull and $"wban".isNull)
      .filter($"latitude".isNotNull and $"longitude".isNotNull)
      .filter($"latitude".as[Double] >= -90 and $"latitude".as[Double] <= 90)
      .filter($"longitude".as[Double] >= -180 and $"longitude".as[Double] <= 180)
      .withColumn("stn", coalesce($"stn", lit("")))
      .withColumn("wban", coalesce($"wban", lit("")))
      .map{
        row =>
          Station(
            row.getAs[String]("stn") + row.getAs[String]("wban"),
            Location(row.getAs[String]("latitude").toDouble,row.getAs[String]("longitude").toDouble)
          )
      }

    val temperaturesDFRaw = sparkSession
      .sqlContext
      .read
      .format("com.databricks.spark.csv")
      .schema(tempSchema)
      .option("header", false)
      .load(fsPath(temperaturesFile))

    val temperatureDS = temperaturesDFRaw
      .filter($"temperatureF".isNotNull)
      .filter($"month".as[Int] >=1 and $"month".as[Int] <= 12)
      .filter($"day".as[Int] >=1 and $"day".as[Int] <= 31)
      .withColumn("stn", coalesce($"stn", lit("")))
      .withColumn("wban", coalesce($"wban", lit("")))
      .map{
          row =>
            DailyTemperature(
              row.getAs[String]("stn") + row.getAs[String]("wban"),
              row.getAs[String]("month").toShort,
              row.getAs[String]("day").toShort,
              {
                val temperatureF = row.getAs[String]("temperatureF").toDouble
                BigDecimal((temperatureF - 32)/1.8).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
              }
            )
        }

   val joined = stationsDS.join(temperatureDS, usingColumn = "stationId")

    joined.printSchema()
    joined.filter($"stationId" === "010010").show
    joined.show(1500)
    stationsDS.show()
    temperatureDS.show()

/*    val temperaturesDS = spark.sqlContext.read.csv(temperaturesFile).as[DailyTemperature]
    println(temperaturesDS.rdd.first())*/

    Seq.empty

  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    ???
  }

}
