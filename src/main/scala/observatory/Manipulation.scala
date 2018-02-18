package observatory

/**
  * 4th milestone: value-added information
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val gridMap: Map[Location, Double] = {
      for {
        lat <- -89 to 90
        lon <- -180 to 179
      } yield Location(lat, lon) -> Visualization.predictTemperature(temperatures, Location(lat, lon))
    }.toMap

    (gridLocation: GridLocation) => gridMap(Location(gridLocation.lat, gridLocation.lon))
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {

    val yearlyAvg = temperaturess.flatten.groupBy(_._1).mapValues{
       locTemps =>
        locTemps.foldLeft(0.0){(acc, locTemp) => acc + locTemp._2 } / locTemps.size

    }
    makeGrid(yearlyAvg)
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val temperaturesGridFunc = makeGrid(temperatures)
    (gridLocation: GridLocation) => temperaturesGridFunc(gridLocation) - normals(gridLocation)
  }


}

