# Renewable Energy Plant System (REPS)

REPS is a sophisticated, Scala-based software simulator designed to mimic the operation of a renewable energy plant. The simulator deals with different types of renewable energy sources such as Solar Panels, Wind Turbines, and Hydropower Plants. REPS provides extensive functionality for adding and removing energy sources, filtering and analyzing energy outputs, saving and reading data from files, detecting issues, and more.

## Features

### 1. Energy Sources

Each energy source (Solar Panel, Wind Turbine, Hydropower Plant) is uniquely identifiable and has its energy outputs and functionality status. Data merging feature allows updating energy outputs from the same source.

### 2. Data Analysis

The simulator includes a Data Analysis class that provides statistical analysis of the energy output data. This includes the mean, median, mode, range, mid-range, and all these statistics together.

### 3. Renewable Energy Plant

A Renewable Energy Plant can consist of multiple energy sources. The plant provides functionality to add or remove energy sources, search data by timestamp, calculate total energy output, save and read data from files, detect issues with energy sources, display the status of all energy sources, and alert of any issues.

### 4. Data Filtering

Data can be filtered based on the hour of the day, day of the month, week of the year, and month of the year. This allows for the analysis of energy outputs based on specific periods of time.

### 5. Storage Management

REPS allows the tracking of the number of data entries stored for each energy source, providing valuable insights into the amount of data being processed and stored.

### 6. Data Adjustment

The simulator allows the adjustment of energy outputs of a source by a certain amount. This can be useful for simulating changes in the energy production of a source.

## Installation

This project is written in Scala and requires a JVM to run. Please ensure that you have Java installed on your machine before proceeding.

## Usage

To use the simulator, instantiate the RenewableEnergyPlant class and populate it with instances of the RenewableEnergySource subclasses (SolarPanel, WindTurbine, HydropowerPlant). From there, you can use the provided methods to manipulate and analyze the energy output data.

## Contribute

We welcome contributions from the open-source community. Feel free to fork the project, make some changes, and submit a pull request. If you find any bugs or have a feature request, please open an issue on GitHub.

## License

This project is open-source and available under the MIT License.

We hope you find REPS useful in your exploration and analysis of renewable energy data. We're excited to see what the community will bring to this project.
