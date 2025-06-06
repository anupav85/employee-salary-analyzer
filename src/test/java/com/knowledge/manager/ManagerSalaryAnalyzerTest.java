package com.knowledge.manager;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ManagerSalaryAnalyzerTest {

    private List<Employee> buildSampleEmployees() {
        return Arrays.asList(
                new Employee(1, "Alice", "CEO", 500000, null),
                new Employee(2, "Bob", "VP", 130000, 1),
                new Employee(3, "Carol", "VP", 120000, 1),
                new Employee(4, "David", "Director", 90000, 2),
                new Employee(5, "Eva", "Director", 95000, 2),
                new Employee(6, "Frank", "Manager", 70000, 4),
                new Employee(7, "Grace", "Manager", 120000, 4),
                new Employee(8, "Hank", "Lead", 50000, 6),
                new Employee(9, "Ivy", "Lead", 52000, 6),
                new Employee(10, "Jack", "Dev", 40000, 8),
                new Employee(11, "Kathy", "Dev", 41000, 8),
                new Employee(12, "Leo", "Dev", 42000, 9),
                new Employee(13, "Mona", "Dev", 43000, 9),
                new Employee(14, "Nina", "QA", 35000, 5),
                new Employee(15, "Oscar", "QA", 34000, 5),
                new Employee(16, "Paul", "Support", 33000, 7),
                new Employee(17, "Quinn", "Support", 32000, 7),
                new Employee(18, "Ruth", "Sales", 31000, 100), // missing manager
                new Employee(19, "Sam", "Eng", 30000, null) // no manager, not CEO
        );
    }

    @Test
    void testAnalyzeManagerSalary_returnsExpectedResults() {
        List<Employee> employees = buildSampleEmployees();
        Map<Integer, Employee> idToEmployee = ManagerSalaryAnalyzer.buildIdToEmployee(employees);
        Map<Integer, List<Employee>> managerToReports = ManagerSalaryAnalyzer.buildManagerToReports(employees);

        List<String> results = ManagerSalaryAnalyzer.analyzeManagerSalary(managerToReports, idToEmployee);

        assertFalse(results.isEmpty(), "Results should not be empty for sample data");
        assertTrue(results.stream().anyMatch(s -> s.contains("UNDERPAID") || s.contains("OVERPAID")),
                "Should contain underpaid or overpaid messages");

        // Specific manager checks
        assertTrue(results.stream().anyMatch(s -> s.contains("ManagerId: 4")), "Should include David (ManagerId 4)");
        assertTrue(results.stream().anyMatch(s -> s.contains("ManagerId: 7")), "Should include Grace (ManagerId 7)");
        assertTrue(results.stream().anyMatch(s -> s.contains("ManagerId: 5")), "Should include Eva (ManagerId 5)");
    }

    @Test
    void testSingleEmployeeToCEO() {
        List<Employee> employees = Collections.singletonList(
                new Employee(1, "Solo", "CEO", 100000, null)
        );
        Map<Integer, Employee> idToEmployee = ManagerSalaryAnalyzer.buildIdToEmployee(employees);
        Map<Integer, List<Employee>> managerToReports = ManagerSalaryAnalyzer.buildManagerToReports(employees);

        // Salary analysis: should yield no results
        List<String> results = ManagerSalaryAnalyzer.analyzeManagerSalary(managerToReports, idToEmployee);
        assertTrue(results.isEmpty(), "No salary analysis for single CEO");

        // Hierarchy: CEO should have 0 managers to CEO
        assertEquals(0, ManagerSalaryAnalyzer.countManagersToCEO(employees.get(0), idToEmployee));

        // Deep hierarchy: should not flag the CEO
        List<Integer> deepIds = new ArrayList<>();
        for (Employee emp : employees) {
            int count = ManagerSalaryAnalyzer.countManagersToCEO(emp, idToEmployee);
            if (count > 4) deepIds.add(emp.id);
        }
        assertTrue(deepIds.isEmpty(), "Single CEO should not be flagged for deep hierarchy");
    }

    @Test
    void testAnalyzeManagerSalary_emptyInput() {
        Map<Integer, Employee> idToEmployee = new HashMap<>();
        Map<Integer, List<Employee>> managerToReports = new HashMap<>();

        List<String> results = ManagerSalaryAnalyzer.analyzeManagerSalary(managerToReports, idToEmployee);
        assertTrue(results.isEmpty(), "No output for empty input");
    }

    @Test
    void testBuildIdToEmployee_emptyList() {
        Map<Integer, Employee> map = ManagerSalaryAnalyzer.buildIdToEmployee(Collections.emptyList());
        assertTrue(map.isEmpty());
    }

    @Test
    void testBuildManagerToReports_emptyList() {
        Map<Integer, List<Employee>> map = ManagerSalaryAnalyzer.buildManagerToReports(Collections.emptyList());
        assertTrue(map.isEmpty());
    }

    @Test
    void testAverageSalary_duplicatesAndEmpty() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "A", "B", 100, null),
                new Employee(2, "C", "D", 100, null)
        );
        assertEquals(100.0, ManagerSalaryAnalyzer.averageSalary(employees));
        assertEquals(0.0, ManagerSalaryAnalyzer.averageSalary(Collections.emptyList()));
    }

    @Test
    void testFindLowestAndHighestSalaryEmployee_duplicatesAndEmpty() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "A", "B", 50, null),
                new Employee(2, "C", "D", 200, null),
                new Employee(3, "E", "F", 50, null)
        );
        assertEquals(1, ManagerSalaryAnalyzer.findLowestSalaryEmployee(employees).id);
        assertEquals(2, ManagerSalaryAnalyzer.findHighestSalaryEmployee(employees).id);
        assertNull(ManagerSalaryAnalyzer.findLowestSalaryEmployee(Collections.emptyList()));
        assertNull(ManagerSalaryAnalyzer.findHighestSalaryEmployee(Collections.emptyList()));
    }

    @Test
    void testPercentDifference_variousCases() {
        assertEquals(25.0, ManagerSalaryAnalyzer.percentDifference(125, 100));
        assertEquals(-20.0, ManagerSalaryAnalyzer.percentDifference(80, 100));
        assertEquals(0.0, ManagerSalaryAnalyzer.percentDifference(100, 0));
        assertEquals(-100.0, ManagerSalaryAnalyzer.percentDifference(0, 100));
    }

    @Test
    void testIsCsvSizeValid() {
        List<Employee> employees = new ArrayList<>();
        for (int i = 1; i <= 1000; i++)
            employees.add(new Employee(i, "A", "B", 100, null));
        assertTrue(ManagerSalaryAnalyzer.isCsvSizeValid(employees, 1000));
        employees.add(new Employee(1001, "C", "D", 200, null));
        assertFalse(ManagerSalaryAnalyzer.isCsvSizeValid(employees, 1000));
    }

    @Test
    void testCountManagersToCEO_variousDepths() {
        List<Employee> employees = buildSampleEmployees();
        Map<Integer, Employee> idToEmployee = ManagerSalaryAnalyzer.buildIdToEmployee(employees);
        assertEquals(5, ManagerSalaryAnalyzer.countManagersToCEO(idToEmployee.get(10), idToEmployee)); // Jack
        assertEquals(3, ManagerSalaryAnalyzer.countManagersToCEO(idToEmployee.get(14), idToEmployee)); // Nina
        assertEquals(1, ManagerSalaryAnalyzer.countManagersToCEO(idToEmployee.get(18), idToEmployee)); // Ruth
        assertEquals(0, ManagerSalaryAnalyzer.countManagersToCEO(idToEmployee.get(1), idToEmployee)); // CEO
    }

    @Test
    void testDeepHierarchyDetection() {
        List<Employee> employees = buildSampleEmployees();
        Map<Integer, Employee> idToEmployee = ManagerSalaryAnalyzer.buildIdToEmployee(employees);
        List<Integer> deepIds = new ArrayList<>();
        for (Employee emp : employees) {
            int count = ManagerSalaryAnalyzer.countManagersToCEO(emp, idToEmployee);
            if (count > 4) deepIds.add(emp.id);
        }
        assertTrue(deepIds.contains(10)); // Jack
        assertTrue(deepIds.contains(11)); // Kathy
        assertFalse(deepIds.contains(14)); // Nina (only 3)
    }


    @Test
    void testEmployeeWithoutManager() {
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", "CEO", 100000, null),
                new Employee(2, "Orphan", "NoManager", 50000, null)
        );
        Map<Integer, Employee> idToEmployee = ManagerSalaryAnalyzer.buildIdToEmployee(employees);
        // Orphan should have 0 managers to CEO, but is not CEO
        assertEquals(0, ManagerSalaryAnalyzer.countManagersToCEO(employees.get(1), idToEmployee));
        // Optionally, assert that only one CEO should exist
        long ceoCount = employees.stream().filter(e -> e.managerId == null).count();
        assertTrue(ceoCount > 1);
        Map<Integer, List<Employee>> managerToReports = ManagerSalaryAnalyzer.buildManagerToReports(employees);
        List<String> results = ManagerSalaryAnalyzer.analyzeManagerSalary(managerToReports, idToEmployee);
        assertTrue(results.stream().anyMatch(e -> e.contains("More than one CEO detected in the company.")));
    }

    @Test
    void testEmployeeWithMultipleManagers() {
        // Simulate by adding the same employee twice with different managers
        List<Employee> employees = Arrays.asList(
                new Employee(1, "Alice", "CEO", 100000, null),
                new Employee(2, "Bob", "Manager1", 90000, 1),
                new Employee(3, "Carol", "Manager2", 90000, 1),
                new Employee(4, "Eve", "MultiManaged", 50000, 2),
                new Employee(4, "Eve", "MultiManaged", 50000, 3) // duplicate ID, different manager
        );
        // This will break your idToEmployee map (duplicate keys)
        Map<Integer, Employee> idToEmployee = ManagerSalaryAnalyzer.buildIdToEmployee(employees);
        Map<Integer, List<Employee>> managerToReports = ManagerSalaryAnalyzer.buildManagerToReports(employees);
        List<String> results = ManagerSalaryAnalyzer.analyzeManagerSalary(managerToReports, idToEmployee);
        assertTrue(results.stream().anyMatch(e -> e.contains("has multiple managers (not supported).")));
    }

    @Test
    void testCircularReportingStructure() {
        // A (1) -> B (2), B (2) -> C (3), C (3) -> A (1)
        List<Employee> employees = Arrays.asList(
                new Employee(1, "A", "Alpha", 10000, 3),
                new Employee(2, "B", "Bravo", 9000, 1),
                new Employee(3, "C", "Charlie", 8000, 2)
        );
        Map<Integer, Employee> idToEmployee = ManagerSalaryAnalyzer.buildIdToEmployee(employees);
        Map<Integer, List<Employee>> managerToReports = ManagerSalaryAnalyzer.buildManagerToReports(employees);
        List<String> results = ManagerSalaryAnalyzer.analyzeManagerSalary(managerToReports, idToEmployee);
        assertTrue(results.stream().anyMatch(e -> e.contains("circular reporting structure")));
    }
}
