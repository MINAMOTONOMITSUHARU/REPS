package Project

import java.io._
import java.time.temporal.{ChronoField, IsoFields}
import scala.io.Source
import scala.util.{Try, Using}
import java.time.{Instant, LocalDateTime, ZoneOffset}


// This abstract class represents a renewable energy source.
abstract class RenewableEnergySource {
  val id: String  // Unique identifier for the energy source.
  val sourceType: String  // Type of the energy source (e.g., Solar, Wind, Hydropower).
  var energyOutputs: List[TimestampedOutput]  // List of timestamped energy outputs.
  val isMalfunctioning: Boolean  // Indicator if the energy source is malfunctioning.
  def energyOutput: Double = energyOutputs.map(_.output).sum  // Function to calculate total energy output.

  // Function to merge data from another RenewableEnergySource.
  def merge(other: RenewableEnergySource): RenewableEnergySource
}

// Class representing a timestamped energy output.
case class TimestampedOutput(timestamp: Instant, output: Double)

// This class represents a Solar Panel as a renewable energy source.
case class SolarPanel(id: String, var energyOutputs: List[TimestampedOutput], isMalfunctioning: Boolean = false)
  extends RenewableEnergySource {
  val sourceType: String = "Solar"  // Type of this energy source.
  override def merge(other: RenewableEnergySource): SolarPanel = other match {  // Function to merge data from another SolarPanel.
    case sp: SolarPanel if sp.id == id =>
      this.copy(energyOutputs = (this.energyOutputs ++ sp.energyOutputs).distinct)
    case _ => throw new IllegalArgumentException("Cannot merge data from different sources.")
  }
}

// This class represents a Wind Turbine as a renewable energy source.
case class WindTurbine(id: String, var energyOutputs: List[TimestampedOutput], isMalfunctioning: Boolean = false)
  extends RenewableEnergySource {
  val sourceType: String = "Wind"  // Type of this energy source.
  override def merge(other: RenewableEnergySource): WindTurbine = other match {  // Function to merge data from another WindTurbine.
    case wt: WindTurbine if wt.id == id =>
      this.copy(energyOutputs = (this.energyOutputs ++ wt.energyOutputs).distinct)
    case _ => throw new IllegalArgumentException("Cannot merge data from different sources.")
  }
}

// This class represents a Hydropower Plant as a renewable energy source.
case class HydropowerPlant(id: String, var energyOutputs: List[TimestampedOutput], isMalfunctioning: Boolean = false)
  extends RenewableEnergySource {
  val sourceType: String = "Hydropower"  // Type of this energy source.
  override def merge(other: RenewableEnergySource): HydropowerPlant = other match {  // Function to merge data from another HydropowerPlant.
    case hp: HydropowerPlant if hp.id == id =>
      this.copy(energyOutputs = (this.energyOutputs ++ hp.energyOutputs).distinct)
    case _ => throw new IllegalArgumentException("Cannot merge data from different sources.")
  }
}

// This class is for performing statistical analysis on data.
class DataAnalysis(data: List[Double]) {
  // Function to calculate mean.
  def mean(): Option[Double] = {
    if (data.isEmpty) None
    else Some(data.sum / data.length)
  }

  // Function to calculate median.
  def median(): Option[Double] = {
    if (data.isEmpty) None
    else {
      val sortedData = data.sorted
      val mid = sortedData.length / 2
      if (sortedData.length % 2 == 0) Some((sortedData(mid) + sortedData(mid - 1)) / 2.0)
      else Some(sortedData(mid))
    }
  }

  // Function to calculate mode.
  def mode(): Option[Double] = {
    if (data.isEmpty) None
    else Some(data.groupBy(identity).maxBy(_._2.size)._1)
  }

  // Function to calculate range.
  def range(): Option[Double] = {
    if (data.isEmpty) None
    else Some(data.max - data.min)
  }

  // Function to calculate mid-range.
  def midRange(): Option[Double] = {
    if (data.isEmpty) None
    else Some((data.max + data.min) / 2)
  }

  // Function to get all statistics.
  def allStats(): (Option[Double], Option[Double], Option[Double], Option[Double], Option[Double]) = {
    (mean(), median(), mode(), range(), midRange())
  }
}

// This class represents a Renewable Energy Plant.
class RenewableEnergyPlant(val energySources: List[RenewableEnergySource] = List()) {

  // Function to search data by timestamp.
  def searchData(timestamp: Instant): List[TimestampedOutput] = {
    energySources.flatMap(source => source.energyOutputs.filter(_.timestamp.equals(timestamp)))
  }

  // Function to add a new energy source to the plant.
  def addEnergySource(energySource: RenewableEnergySource): RenewableEnergyPlant = {
    new RenewableEnergyPlant(energySource :: energySources)
  }

  // Function to calculate total energy output of the plant.
  def totalEnergyOutput(): Double = {
    energySources.foldLeft(0.0)(_ + _.energyOutput)
  }

  // Function to save data to a file.
  def saveDataToFile(filename: String): Try[Unit] = {
    Try {
      Using(new BufferedWriter(new FileWriter(new File(filename)))) { bw =>
        bw.write("sourceType, id, timestamp, output\n") // write headers
        energySources.foreach { source =>
          source.energyOutputs.foreach { output =>
            bw.write(s"${source.sourceType}, ${source.id}, ${output.timestamp}, ${output.output}\n")
          }
        }
      }
    }.flatten
  }


  // Function to read data from a file and build a RenewableEnergyPlant object.
  def readDataFromFile(filename: String): Either[String, RenewableEnergyPlant] = {
    Try(Source.fromFile(filename).getLines().toList).toEither.left.map(_.getMessage).flatMap { lines =>
      val header = lines.head.split(",").map(_.trim)
      val dataLines = lines.tail
      val newSources = dataLines.flatMap { line =>
        val cols = line.split(",").map(_.trim)
        if (cols.length != header.length) {
          None // ignore lines with incorrect number of columns
        } else {
          val sourceType = cols(0)
          val id = cols(1)
          val energyOutputs = cols(2).split("\\|").toList.flatMap { s =>
            val parts = s.split(",").map(_.trim)
            Try {
              val timestamp = Instant.parse(parts(0))
              val output = parts(1).toDouble
              TimestampedOutput(timestamp, output)
            }.toOption
          }

          // Construct the appropriate RenewableEnergySource object based on the sourceType.
          sourceType match {
            case "Solar" => Some(SolarPanel(id, energyOutputs))
            case "Wind" => Some(WindTurbine(id, energyOutputs))
            case "Hydropower" => Some(HydropowerPlant(id, energyOutputs))
            case _ => None
          }
        }
      }

      // Return a new RenewableEnergyPlant if all data lines were successfully parsed, otherwise return an error message.
      if (newSources.size == dataLines.size) Right(new RenewableEnergyPlant(newSources ++ energySources))
      else Left("Invalid data in the file.")
    }
  }

  // Function to analyze the energy output data of all energy sources.
  def analyzeData(): List[DataAnalysis] = {
    energySources.map(s => new DataAnalysis(s.energyOutputs.map(_.output)))
  }

  // Function to detect issues with the energy sources (low energy output or malfunctioning).
  def detectIssues(threshold: Double): List[RenewableEnergySource] = {
    energySources.filter(source => source.energyOutput < threshold || source.isMalfunctioning) // added check for malfunction
  }

  // Function to display the status of all energy sources.
  def displayStatus(): Unit = {
    energySources.foreach { source =>
      println(s"${source.sourceType} (${source.id}) has generated ${source.energyOutput} units of energy.")
    }
  }

  // Function to alert of any issues with the energy sources.
  def alertIssues(threshold: Double): Unit = {
    val issues = detectIssues(threshold)
    if (issues.isEmpty) {
      println("No issues detected.")
    } else {
      issues.foreach { source =>
        if (source.energyOutput < threshold) {
          println(s"Alert: ${source.sourceType} (${source.id}) has low energy output: ${source.energyOutput} units.")
        }
        if (source.isMalfunctioning) {
          println(s"Alert: ${source.sourceType} (${source.id}) is malfunctioning.") // added alert for malfunction
        }
      }
    }
  }


  // Function to filter the energy outputs based on the hour of the day (UTC).
  def filterByHour(hour: Int): RenewableEnergyPlant = {
    val filteredSources = energySources.map { source =>
      val filteredOutputs = source.energyOutputs.filter { output =>
        output.timestamp.atZone(ZoneOffset.UTC).getHour == hour
      }
      // Recreate the source with the filtered outputs.
      source match {
        case _: SolarPanel => SolarPanel(source.id, filteredOutputs)
        case _: WindTurbine => WindTurbine(source.id, filteredOutputs)
        case _: HydropowerPlant => HydropowerPlant(source.id, filteredOutputs)
      }
    }
    new RenewableEnergyPlant(filteredSources)
  }

  // Function to filter the energy outputs based on the day of the month (UTC).
  def filterByDay(day: Int): RenewableEnergyPlant = {
    val filteredSources = energySources.map { source =>
      val filteredOutputs = source.energyOutputs.filter { output =>
        output.timestamp.atZone(ZoneOffset.UTC).getDayOfMonth == day
      }
      // Recreate the source with the filtered outputs.
      source match {
        case _: SolarPanel => SolarPanel(source.id, filteredOutputs)
        case _: WindTurbine => WindTurbine(source.id, filteredOutputs)
        case _: HydropowerPlant => HydropowerPlant(source.id, filteredOutputs)
      }
    }
    new RenewableEnergyPlant(filteredSources)
  }

  // Function to filter the energy outputs based on the week of the year (UTC).
  def filterByWeek(week: Int): RenewableEnergyPlant = {
    val filteredSources = energySources.map { source =>
      val filteredOutputs = source.energyOutputs.filter { output =>
        output.timestamp.atZone(ZoneOffset.UTC).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == week
      }
      // Recreate the source with the filtered outputs.
      source match {
        case _: SolarPanel => SolarPanel(source.id, filteredOutputs)
        case _: WindTurbine => WindTurbine(source.id, filteredOutputs)
        case _: HydropowerPlant => HydropowerPlant(source.id, filteredOutputs)
      }
    }
    new RenewableEnergyPlant(filteredSources)
  }

  // Function to filter the energy outputs based on the month of the year (UTC).
  def filterByMonth(month: Int): RenewableEnergyPlant = {
    val filteredSources = energySources.map { source =>
      val filteredOutputs = source.energyOutputs.filter { output =>
        output.timestamp.atZone(ZoneOffset.UTC).get(ChronoField.MONTH_OF_YEAR) == month
      }
      // Recreate the source with the filtered outputs.
      source match {
        case _: SolarPanel => SolarPanel(source.id, filteredOutputs)
        case _: WindTurbine => WindTurbine(source.id, filteredOutputs)
        case _: HydropowerPlant => HydropowerPlant(source.id, filteredOutputs)
      }
    }
    new RenewableEnergyPlant(filteredSources)
  }

  // Function to find an energy source by its id.
  def searchById(id: String): Option[RenewableEnergySource] = {
    energySources.find(_.id == id)
  }

  // Function to remove an energy source by its id.
  def removeEnergySource(id: String): RenewableEnergyPlant = {
    new RenewableEnergyPlant(energySources.filterNot(_.id == id))
  }

  // Function to adjust the energy output of a source by a certain amount.
  def adjustEnergyOutput(id: String, adjustment: Double): RenewableEnergyPlant = {
    val adjustedSources = energySources.map { source =>
      // Check if the source id matches the given id.
      if (source.id == id) {
        // If it matches, adjust the energy output.
        val adjustedOutputs = source.energyOutputs.map { output =>
          TimestampedOutput(output.timestamp, output.output + adjustment)
        }
        // Recreate the source with the adjusted outputs.
        source match {
          case _: SolarPanel => SolarPanel(source.id, adjustedOutputs)
          case _: WindTurbine => WindTurbine(source.id, adjustedOutputs)
          case _: HydropowerPlant => HydropowerPlant(source.id, adjustedOutputs)
        }
      } else source // If the id doesn't match, keep the source as is.
    }
    new RenewableEnergyPlant(adjustedSources)
  }

  // Function to display the number of data entries stored for each energy source.
  def displayStorage(): Unit = {
    val totalStorage = energySources.map(_.energyOutputs.length).sum
    println(s"Total storage: $totalStorage entries.")
    energySources.foreach { source =>
      println(s"${source.sourceType} (${source.id}) has stored ${source.energyOutputs.length} entries of data.")
    }
  }

  // Function to sort the energy outputs of each source by timestamp.
  def sortDataByTimestamp(): RenewableEnergyPlant = {
    val sortedSources = energySources.map { source =>
      // Sort the outputs by timestamp and recreate the source.
      val sortedOutputs = source.energyOutputs.sortBy(_.timestamp)
      source match {
        case _: SolarPanel => SolarPanel(source.id, sortedOutputs, source.isMalfunctioning)
        case _: WindTurbine => WindTurbine(source.id, sortedOutputs, source.isMalfunctioning)
        case _: HydropowerPlant => HydropowerPlant(source.id, sortedOutputs, source.isMalfunctioning)
      }
    }
    new RenewableEnergyPlant(sortedSources)
  }

}









