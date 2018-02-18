package observatory

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CapstoneSuite
  extends ExtractionTest
    with VisualizationTest
    with InteractionTest
    with ManipulationTest
    with Visualization2Test
    with Interaction2Test {


  test("asd") {
/*      val temperatures = Extraction.locateTemperatures(1975,"/stations.csv","/1975.csv")
      val temperaturesAvg = Extraction.locationYearlyAverageRecords(temperatures)
      val temp = Visualization.predictTemperature(temperaturesAvg,Location(29.2,13))*/
     // Visualization.interpolateColor(Array((0.0,Color(255,0,0)), (1.0,Color(0,0,255))),9.0)
     //Visualization.interpolateColor(Array((-1.0,Color(255,0,0)), (2.147483647E9,Color(0,0,255))),9.0)
   // val n = Visualization.interpolateColor(List((1.0,Color(255,0,0)), (63.39961771068977,Color(0,0,255))), value = -9.0)

    val temperatures = List((Location(45.0,-90.0),18.16683663944329), (Location(-45.0,0.0),-1.0))
    val colors = List((18.16683663944329,Color(255,0,0)), (-1.0,Color(0,0,255)))

    val lat = 90
    val lon = -180

   val m=  Visualization.interpolateColor(colors ,Visualization.predictTemperature(temperatures,Location(lat,lon)))

    assert(true)
  }
}

