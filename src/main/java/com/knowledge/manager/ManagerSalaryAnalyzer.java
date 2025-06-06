package com.knowledge.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ManagerSalaryAnalyzer {

    public static final int MAX_CSV_SIZE = 1000;

    public static void main(String[] args) {
        List<Employee> employees = readEmployeesFromCsv(args.length > 0 ? args[0] : "employees.csv");
        if (employees == null || employees.isEmpty()) return;

        if (!isCsvSizeValid(employees, MAX_CSV_SIZE)) {
            System.out.println("Input CSV contains " + employees.size() + " entries.");
            System.out.println("Please split the file so that each part contains no more than " + MAX_CSV_SIZE + " entries.");
            return;
        }

        Map<Integer, Employee> idToEmployee = buildIdToEmployee(employees);
        Map<Integer, List<Employee>> managerToReports = buildManagerToReports(employees);

        analyzeManagerSalary(managerToReports, idToEmployee);
        printEmployeesWithDeepHierarchy(employees, idToEmployee, 4);
    }

    public static List<Employee> readEmployeesFromCsv(String resourceName) {
        List<Employee> employees = new ArrayList<>();
        try (InputStream is = ManagerSalaryAnalyzer.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                System.err.println("Could not find " + resourceName + " in resources folder.");
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    String[] parts = line.split(",", -1);
                    if (parts.length < 5) continue;
                    int id = Integer.parseInt(parts[0]);
                    String firstName = parts[1];
                    String lastName = parts[2];
                    double salary = Double.parseDouble(parts[3]);
                    Integer managerId = parts[4].isEmpty() ? null : Integer.parseInt(parts[4]);
                    employees.add(new Employee(id, firstName, lastName, salary, managerId));
                    if (employees.size() > MAX_CSV_SIZE) break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public static boolean isCsvSizeValid(List<Employee> employees, int maxSize) {
        return employees.size() <= maxSize;
    }

    public static Map<Integer, Employee> buildIdToEmployee(List<Employee> employees) {
        Map<Integer, Employee> map = new HashMap<>();
        for (Employee e : employees) map.put(e.id, e);
        return map;
    }

    public static Map<Integer, List<Employee>> buildManagerToReports(List<Employee> employees) {
        Map<Integer, List<Employee>> map = new HashMap<>();
        for (Employee e : employees) {
            if (e.managerId != null) {
                map.computeIfAbsent(e.managerId, k -> new ArrayList<>()).add(e);
            }
        }
        return map;
    }

    public static List<String> analyzeManagerSalary(
            Map<Integer, List<Employee>> managerToReports,
            Map<Integer, Employee> idToEmployee) {

        List<String> results = new ArrayList<>();
        Set<Integer> subordinateIds = new HashSet<>();
        Set<Integer> employeesWithMultipleManagers = new HashSet<>();

        // 1. Detect employees with multiple managers (if data allows)
        hasMultipleManagers(managerToReports, subordinateIds, employeesWithMultipleManagers, results);

        // 2. Detect cycles (circular reporting)
        cycleDetected(idToEmployee, results);

        // 3. Detect Multi CEO
        hasMultiCEO(idToEmployee, results);

        // 4. Detect Single reportee to CEO
        onlyReporteeToCEO(idToEmployee, results);

        // 5. Salary analysis and employees with no subordinates
        for (Map.Entry<Integer, List<Employee>> entry : managerToReports.entrySet()) {
            int managerId = entry.getKey();
            List<Employee> reports = entry.getValue();
            Employee manager = idToEmployee.get(managerId);
            if (manager == null || isCEO(manager, idToEmployee)) {
                continue;
            }

            if (reports.isEmpty()) {
                results.add("ManagerId: " + managerId + " (" + manager.firstName + " " + manager.lastName + ") has no subordinates.");
                continue;
            }

            double managerSalary = manager.salary;
            double sum = 0.0;
            int minId = -1, maxId = -1;
            double minSalary = Double.MAX_VALUE, maxSalary = Double.MIN_VALUE;
            for (Employee report : reports) {
                sum += report.salary;
                if (report.salary < minSalary) {
                    minSalary = report.salary;
                    minId = report.id;
                }
                if (report.salary > maxSalary) {
                    maxSalary = report.salary;
                    maxId = report.id;
                }
            }
            double avgSubordinateSalary = sum / reports.size();
            double percentDiff = ((managerSalary - avgSubordinateSalary) / avgSubordinateSalary) * 100.0;
            double salaryDiff = managerSalary - avgSubordinateSalary;
            double minShouldEarn = avgSubordinateSalary * 1.2;
            double maxShouldEarn = avgSubordinateSalary * 1.5;

            if (managerSalary < minShouldEarn) {
                double diff = minShouldEarn - managerSalary;
                results.add("ManagerId: " + managerId +
                        " (" + manager.firstName + " " + manager.lastName + ")" +
                        " is UNDERPAID by " + Math.round(diff) +
                        " (earns " + Math.round(managerSalary) +
                        ", should earn at least " + Math.round(minShouldEarn) + ")" +
                        ", average subordinate salary: " + Math.round(avgSubordinateSalary)
//                        + ", lowest subordinate: " + Math.round(minSalary) + " (" + minId + ")" +
//                        ", highest subordinate: " + Math.round(maxSalary) + " (" + maxId + ")" +
//                        ", difference: " + Math.round(salaryDiff) +
//                        ", percent difference: " + Math.round(percentDiff) + "%"
                );
            } else if (managerSalary > maxShouldEarn) {
                double diff = managerSalary - maxShouldEarn;
                results.add("ManagerId: " + managerId +
                        " (" + manager.firstName + " " + manager.lastName + ")" +
                        " is OVERPAID by " + Math.round(diff) +
                        " (earns " + Math.round(managerSalary) +
                        ", should earn no more than " + Math.round(maxShouldEarn) + ")" +
                        ", average subordinate salary: " + Math.round(avgSubordinateSalary)
//                        + ", lowest subordinate: " + Math.round(minSalary) + " (" + minId + ")" +
//                        ", highest subordinate: " + Math.round(maxSalary) + " (" + maxId + ")" +
//                        ", difference: " + Math.round(salaryDiff) +
//                        ", percent difference: " + Math.round(percentDiff) + "%"
              );
            }
        }
        results.forEach(System.out::println);
        return results;
    }

    private static boolean onlyReporteeToCEO(Map<Integer, Employee> idToEmployee, List<String> results) {
        for (Employee e : idToEmployee.values()) {
            if (idToEmployee.size() == 2 && !isCEO(e, idToEmployee)) {
                results.add("WARNING: EmployeeId: " + e.id + " (" + e.firstName + " " + e.lastName + ") has no subordinates and is not CEO.");
            }
        }
        return !results.isEmpty();
    }

    private static boolean hasMultiCEO(Map<Integer, Employee> idToEmployee, List<String> results) {
        long ceoCount = idToEmployee.values().stream().filter(e -> e.managerId == null).count();
            if (ceoCount>1) {
                results.add("WARNING: More than one CEO detected in the company.");
            }
        return !results.isEmpty();
    }


    private static boolean cycleDetected(Map<Integer, Employee> idToEmployee, List<String> results) {
        for (Employee e : idToEmployee.values()) {
            if (hasCycle(e, idToEmployee, new HashSet<>())) {
                results.add("WARNING: EmployeeId: " + e.id + " (" + e.firstName + " " + e.lastName + ") is in a circular reporting structure.");
            }
        }
        return !results.isEmpty();
    }

    private static boolean hasMultipleManagers(Map<Integer, List<Employee>> managerToReports, Set<Integer> subordinateIds, Set<Integer> employeesWithMultipleManagers, List<String> results) {
        for (List<Employee> reports : managerToReports.values()) {
            for (Employee e : reports) {
                if (!subordinateIds.add(e.id)) {
                    employeesWithMultipleManagers.add(e.id);
                }
            }
        }
        for (Integer multiId : employeesWithMultipleManagers) {
            results.add("WARNING: EmployeeId: " + multiId + " has multiple managers (not supported).");
        }
        return !results.isEmpty();
    }

    // Helper to detect cycles using DFS
    private static boolean hasCycle(Employee e, Map<Integer, Employee> idToEmployee, Set<Integer> path) {
        Integer managerId = e.managerId;
        while (managerId != null) {
            if (!path.add(managerId)) return true; // cycle detected
            Employee mgr = idToEmployee.get(managerId);
            if (mgr == null) break;
            managerId = mgr.managerId;
        }
        return false;
    }

    // Helper to identify CEO (no manager, and only one CEO in company)
    private static boolean isCEO(Employee e, Map<Integer, Employee> idToEmployee) {
        return e.managerId == null && idToEmployee.values().stream().filter(emp -> emp.managerId == null).count() == 1;
    }


    public static double averageSalary(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Employee e : employees) sum += e.salary;
        return sum / employees.size();
    }

    public static double percentDifference(double managerSalary, double avgSubSalary) {
        if (avgSubSalary == 0.0) return 0.0;
        return ((managerSalary - avgSubSalary) / avgSubSalary) * 100.0;
    }

    public static Employee findLowestSalaryEmployee(List<Employee> employees) {
        return employees.stream().min(Comparator.comparingDouble(e -> e.salary)).orElse(null);
    }

    public static Employee findHighestSalaryEmployee(List<Employee> employees) {
        return employees.stream().max(Comparator.comparingDouble(e -> e.salary)).orElse(null);
    }

    public static void printEmployeesWithDeepHierarchy(List<Employee> employees, Map<Integer, Employee> idToEmployee, int threshold) {
        for (Employee emp : employees) {
            int count = countManagersToCEO(emp, idToEmployee);
            if (count > threshold) {
                System.out.println("EmployeeId: " + emp.id +
                        " (" + emp.firstName + " " + emp.lastName + ")" +
                        " has " + count + " managers between them and the CEO.");
            }
        }
    }

    public static int countManagersToCEO(Employee emp, Map<Integer, Employee> idToEmployee) {
        int count = 0;
        Integer managerId = emp.managerId;
        while (managerId != null) {
            count++;
            Employee mgr = idToEmployee.get(managerId);
            if (mgr == null) break;
            managerId = mgr.managerId;
        }
        return count;
    }
}
