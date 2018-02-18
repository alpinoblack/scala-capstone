package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    val CellPoint(x, y) = point
    d00 * (1 - x) * (1 - y) + d10 * x * (1 - y) + d01 * (1 - x) * y + d11 * x * y
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {
    import Visualization._

    val imgArr = new Array[Pixel](256*256)

    val Tile(offSetX, offSetY, zoom) = tile

    for (idxX <- 0 until 256)
      for (idxY <- 0 until 256) {
        val newColor = interpolateColor(colors ,grid(Tile(offSetX*256 + idxX, offSetY*256 + idxY, zoom + 8).toLocation.toGridLocation))
        imgArr(idxX + idxY*256) = Pixel(newColor.red, newColor.green, newColor.blue, 127)
      }

    Image(256,256,imgArr)
  }

}
