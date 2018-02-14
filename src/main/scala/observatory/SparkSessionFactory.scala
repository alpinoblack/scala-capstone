package observatory
import org.apache.spark.sql.SparkSession

/**
  * Created by Or on 1/29/2018.
  */
object SparkSessionFactory {

  val sparkSession: SparkSession =
    SparkSession
      .builder()
      .appName("Time Usage")
      .config("spark.master", "local")
      .getOrCreate()

}
