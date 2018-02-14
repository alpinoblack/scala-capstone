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
      Extraction.locateTemperatures(1975,"/stations.csv","/1975.csv")
    assert(true)
  }
}

