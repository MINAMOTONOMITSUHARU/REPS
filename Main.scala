//Qihang Zhang
//Wenbin Zhou
//Zexu Zhang
package Project

import java.time.Instant
import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    // Create some TimestampedOutputs
    val now = Instant.now()
    val random = new java.util.Random()
    val outputs1 = (1 to 100).map(_ => TimestampedOutput(now.plusSeconds(random.nextInt(10000)), random.nextDouble())).toList
    val outputs2 = (1 to 100).map(_ => TimestampedOutput(now.plusSeconds(random.nextInt(10000)), random.nextDouble())).toList
    val outputs3 = (1 to 100).map(_ => TimestampedOutput(now.plusSeconds(random.nextInt(10000)), random.nextDouble())).toList

    //use case 1: Create some energy sources
    val solarPanel = SolarPanel("SP1", outputs1)
    val windTurbine = WindTurbine("WT1", outputs2)
    val hydropowerPlant = HydropowerPlant("HP1", outputs3)

    // Create a RenewableEnergyPlant and add energy sources
    val plant = new RenewableEnergyPlant()
      .addEnergySource(solarPanel)
      .addEnergySource(windTurbine)
      .addEnergySource(hydropowerPlant)

    // Display the status
    plant.displayStatus()

    // Display the storage
    plant.displayStorage()

    //use case 2: Save data to a file
    plant.saveDataToFile("data.csv")

    //use case 3: Read data from a file
    val plant2 = plant.readDataFromFile("data.csv")
    plant2 match {
      case Right(plant) =>
        for (line <- Source.fromFile("data.csv").getLines) {
          println(line)
        }
      case Left(error) =>
        println(s"Error reading plant data: $error")
    }

    //use case 4: search
    println(plant.searchById("HP1"))
    println(plant.sortDataByTimestamp())

    //sort
    val sortedPlant = plant.sortDataByTimestamp()
    sortedPlant.energySources.foreach { source =>
      println(s"Energy outputs for ${source.sourceType} (${source.id}):")
      source.energyOutputs.foreach(output => println(s"${output.timestamp} -> ${output.output}"))
    }

    //filter by hour 22
    val plantFilteredByHour = plant.filterByHour(22)
    plantFilteredByHour.energySources.foreach { source =>
      println(s"Energy outputs for ${source.sourceType} (${source.id}):")
      source.energyOutputs.foreach(output => println(s"${output.timestamp} -> ${output.output}"))
    }

    // Analyze the data
    val analysis = plant.analyzeData()
    analysis.foreach { a =>
      val (mean, median, mode, range, midRange) = a.allStats()
      println(s"Mean: $mean, Median: $median, Mode: $mode, Range: $range, Midrange: $midRange")
    }

    //use case 5: Check for issues
    plant.alertIssues(50)
  }
}


