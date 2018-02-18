package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import org.apache.log4j.Logger

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  def isAntipode(loc1: Location, loc2: Location): Boolean = {
   val loc1Antipode = Location(-loc1.lat, if (loc1.lon < 0) loc1.lon + 180 else -(360 - (loc1.lon + 180)))
/*   if (loc1 != Location(0.0,0.0)){
      Logger.getLogger("observatory.Visualiztion").warn(s"antipode for $loc1 is $loc1Antipode")
    }*/
   loc1Antipode == loc2
  }

  def inverseDistCalc(loc1: Location, loc2: Location): Double = {
    import scala.math._

    if (loc1 == loc2) {
      0
    } else if (isAntipode(loc1, loc2)) {
      Pi * earthRadiusMeter
    } else {
      acos(sin(loc1.lat.toRadians)*sin(loc2.lat.toRadians) + cos(loc1.lat.toRadians)*cos(loc2.lat.toRadians)*cos(abs(loc1.lon - loc2.lon).toRadians)) * earthRadiusMeter
    }
  }

  val earthRadiusMeter = 6371

  val inverseDistPFactor = 3


  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location     Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  //TODO handle temperatures is Nil
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {

    case class InverseDistAcc(inverseDistNumer: Double, inverseDistDeNom: Double){
      def +(that: InverseDistAcc): InverseDistAcc = {
        InverseDistAcc(this.inverseDistNumer + that.inverseDistNumer, this.inverseDistDeNom + that.inverseDistDeNom)
      }
    }

    def interpolateTemp(temperatures: List[(Location, Temperature)], inverseDistAcc: InverseDistAcc): Temperature = {
      temperatures match {
        case Nil => inverseDistAcc.inverseDistNumer/inverseDistAcc.inverseDistDeNom
        case (loc, temperature) :: tail =>
          inverseDistCalc(loc,location) match {
            case dist if dist < 1 => Logger.getLogger("observatory.Visualization").warn("this is the distance " + dist) ;temperature
            case dist =>
              val normDist = 1 / math.pow(dist,inverseDistPFactor)
              interpolateTemp(tail, inverseDistAcc + InverseDistAcc(normDist*temperature,normDist) )
          }

      }
    }
    interpolateTemp(temperatures.toList, InverseDistAcc(0.0,0.0))
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {

    def linearInterpolation(pointLow: (Temperature, Int), pointHigh: (Temperature, Int)): Int = {
      math.round(
      pointLow._2 + (value - pointLow._1)*((pointHigh._2 - pointLow._2)/(pointHigh._1 - pointLow._1))
      ).toInt

    }

    points.find(_._1 == value) match {
      case Some((_, color)) => color
      case _ =>
        val (smaller, greater) = points.toList.sortBy(_._1).partition(_._1 < value)
        (smaller.reverse.headOption, greater.headOption) match {
          case (Some(scaleLow), Some(scaleHigh)) =>
            Color(
              red = linearInterpolation((scaleLow._1, scaleLow._2.red), (scaleHigh._1, scaleHigh._2.red)),
              blue = linearInterpolation((scaleLow._1, scaleLow._2.blue), (scaleHigh._1, scaleHigh._2.blue)),
              green = linearInterpolation((scaleLow._1, scaleLow._2.green), (scaleHigh._1, scaleHigh._2.green))
            )
          case (Some(scaleLow), None) => scaleLow._2
          case (None, Some(scaleHigh)) => scaleHigh._2
          case _ => Color(0,0,0)
        }




/*        points
        .groupBy(_._1)
        .mapValues(_.head._2)
        .toSeq
        .sortBy(_._1)
        .sliding(2).find {
        scale => scale.head._1 <= value && scale.last._1 >= value
      }.map {
        /*      case scaleLow :: scaleHigh :: Nil =>
                Color(
                  red = linearInterpolation((scaleLow._1, scaleLow._2.red), (scaleHigh._1, scaleHigh._2.red)),
                  blue = linearInterpolation((scaleLow._1, scaleLow._2.blue), (scaleHigh._1, scaleHigh._2.blue)),
                  green = linearInterpolation((scaleLow._1, scaleLow._2.green), (scaleHigh._1, scaleHigh._2.green))
                )*/
        scale =>
          val scaleLow = scale.head
          val scaleHigh = scale.last
          Color(
            red = linearInterpolation((scaleLow._1, scaleLow._2.red), (scaleHigh._1, scaleHigh._2.red)),
            blue = linearInterpolation((scaleLow._1, scaleLow._2.blue), (scaleHigh._1, scaleHigh._2.blue)),
            green = linearInterpolation((scaleLow._1, scaleLow._2.green), (scaleHigh._1, scaleHigh._2.green))
          )
      }.getOrElse{
        if (value <= points.head._1 ) points.head._2 else points.last._2
      }*/
    }


  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {

/*    Logger.getLogger("observatory.Visualization").warn("temperature " + temperatures)
    Logger.getLogger("observatory.Visualization").warn("colors "+ colors)*/


   // val buffImage = new BufferedImage(360, 180, BufferedImage.TYPE_INT_RGB)
    val imgArray = new Array[Pixel](360*180)
    for (x <- 0 until 360) //longitude
      for (y <- 0 until 180) { //latitude
        val lat = 90 - y
        val lon = x - 180

        val newColor = interpolateColor(colors ,predictTemperature(temperatures,Location(lat,lon)))

        imgArray(x + y*360) = Pixel(newColor.red, newColor.green, newColor.blue, 255)

/*        def rgba(r: Int, g: Int, b: Int): Int = {
         0 | (r << 16) | (g << 8) | (b << 0)
        }
        if (x ==0 && y ==90) {
          Logger.getLogger("observatory.Visualization").warn(newColor)
        }
        buffImage.setRGB(x,y, rgba(newColor.red, newColor.green, newColor.blue) )*/
      }
    //buffImage
    Image(360,180,imgArray)
  }

}

