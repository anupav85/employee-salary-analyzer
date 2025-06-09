Employee Salary Analyzer
---

This application is a simple Java application that analyzes employee salary data. It reads a CSV file containing
employee information, calculates the average salary, and provides insights into the salary distribution.

## Features

- Read employee data from a CSV file
- Calculate average salary
- Identify the highest and lowest salaries
- Generate a report (Log) of the analysis based on requirements

## Problem Statement

BIG COMPANY is employing a lot of employees. Company would like to analyze its organizational structure and identify
potential improvements. Board wants to make sure that every manager earns at least 20% more than the average salary of
its direct subordinates, but no more than 50% more than that average. Company wants to avoid too long reporting lines,
therefore we would like to identify all employees which have more than 4 managers between them and the CEO.

Find the following:

- which managers earn less than they should, and by how much
- which managers earn more than they should, and by how much
- which employees have a reporting line which is too long, and by how much

## Technologies Used

- Java SE
- JUnit for testing

## How to Run

1. Clone the repository and run maven goal to package:
   ```bash
   git clone https://github.com/anupav85/employee-salary-analyzer.git
   ```
   ```bash
   mvn package
   ```

2. Navigate to the project target directory:
   ```bash
   cd employee-salary-analyzer/target
   ```

3. Run the application main class: 
   ```bash
   java -jar .\employee-analyzer-1.0-SNAPSHOT.jar
   ```
   Or do you have a different input file, you can pass it as a command line argument:
   ```bash
   java -jar .\employee-analyzer-1.0-SNAPSHOT.jar <path_to_your_input_file>
   ```

  Note: Jar file generated is also uploaded to root directory.

## Input Data

The application expects a CSV file named `employees.csv` in the '/resources'. You can also pass as a command line
argument while running application. If you don't provide one explicitly application uses default input file in the '
/resources' folder.
The CSV file should have the following columns:

- Id: Unique identifier for each employee
- firstname: First name of the employee
- lastname: Last name of the employee
- salary: Salary of the employee
- manager_id: ID of the employee's manager (empty if the employee is a CEO)

## Output

The application will output the following:

- which managers earn less than they should, and by how much
- which managers earn more than they should, and by how much
- which employees have a reporting line which is too long, and by how much

## Assumptions & Usecases covered

- The CSV file is well-formed and contains valid data. It supports upto 1000 employee entries.
- The salary values are numeric and positive.
- The manager_id for the CEO is null/empty.
- The application does not enforce a single CEO, results will include messages with WARNING if such structures are
  detected.
- Circular reporting structures, results will include messages with WARNING if such structures are detected.
- Employees without managers, results will include messages with WARNING if such structures are detected.
- Employees with multiple managers, results will include messages with WARNING if such
  structures are detected.

