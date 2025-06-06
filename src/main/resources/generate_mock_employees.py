import csv
import random

FIRST_NAMES = [
    "Alice", "Bob", "Carol", "David", "Eve", "Frank", "Grace", "Hank", "Ivy", "Jack",
    "Kate", "Liam", "Mia", "Nina", "Oscar", "Paul", "Quinn", "Ruth", "Sam", "Tina"
]
LAST_NAMES = [
    "Smith", "Jones", "White", "Brown", "Green", "Black", "Wilson", "Moore", "Taylor", "Anderson",
    "Thomas", "Jackson", "Lee", "Walker", "Hall", "Allen", "Wright", "King", "Scott", "Young"
]

def generate_employee_data(num_rows, output_file='mock_employees.csv'):
    # Ensure at least 15 rows for deep hierarchy and salary logic
    if num_rows < 15:
        raise ValueError("Please specify at least 15 rows for meaningful hierarchy and salary conditions.")

    employees = []
    id_counter = 1

    # --- CEO ---
    ceo = {
        'Id': id_counter,
        'firstName': 'Alice',
        'lastName': 'Smith',
        'salary': 250000,
        'managerId': ''
    }
    employees.append(ceo)
    id_counter += 1

    # --- Two top-level managers under CEO ---
    manager_low_salary = {
        'Id': id_counter,
        'firstName': 'Bob',
        'lastName': 'Jones',
        'salary': 10000,  # Will be <20% of subordinates' avg
        'managerId': 1
    }
    id_counter += 1

    manager_high_salary = {
        'Id': id_counter,
        'firstName': 'Carol',
        'lastName': 'White',
        'salary': 150000,  # Will be >50% of subordinates' avg
        'managerId': 1
    }
    id_counter += 1

    employees.extend([manager_low_salary, manager_high_salary])

    # --- Subordinates for manager_low_salary (Bob) ---
    sub_bob = []
    for fname, lname, sal in [("David", "Brown", 60000), ("Eve", "Green", 65000), ("Frank", "Black", 70000)]:
        sub_bob.append({
            'Id': id_counter,
            'firstName': fname,
            'lastName': lname,
            'salary': sal,
            'managerId': manager_low_salary['Id']
        })
        id_counter += 1
    employees.extend(sub_bob)

    # --- Subordinates for manager_high_salary (Carol) ---
    sub_carol = []
    for fname, lname, sal in [("Grace", "Wilson", 40000), ("Hank", "Moore", 35000), ("Ivy", "Taylor", 30000)]:
        sub_carol.append({
            'Id': id_counter,
            'firstName': fname,
            'lastName': lname,
            'salary': sal,
            'managerId': manager_high_salary['Id']
        })
        id_counter += 1
    employees.extend(sub_carol)

    # --- Deep hierarchy chain under one of Carol's subordinates for 4+ levels ---
    prev_id = sub_carol[0]['Id']
    deep_chain = []
    chain_names = [
        ("Jack", "Anderson", 25000),
        ("Kate", "Thomas", 20000),
        ("Liam", "Jackson", 15000),
        ("Mia", "Lee", 10000),
        ("Nina", "Walker", 8000),
        ("Oscar", "Hall", 6000)
    ]
    for fname, lname, sal in chain_names:
        deep_chain.append({
            'Id': id_counter,
            'firstName': fname,
            'lastName': lname,
            'salary': sal,
            'managerId': prev_id
        })
        prev_id = id_counter
        id_counter += 1
    employees.extend(deep_chain)

    # --- Add more employees to reach num_rows, assign random managers ---
    while len(employees) < num_rows:
        fname = random.choice(FIRST_NAMES)
        lname = random.choice(LAST_NAMES)
        # Randomly pick a manager from existing employees (excluding the last in deep chain to avoid loops)
        possible_managers = [emp['Id'] for emp in employees if emp['Id'] != prev_id]
        manager_id = random.choice(possible_managers)
        # Salary: random between 5,000 and 80,000
        salary = random.randint(5000, 80000)
        employees.append({
            'Id': id_counter,
            'firstName': fname,
            'lastName': lname,
            'salary': salary,
            'managerId': manager_id
        })
        id_counter += 1

    # --- Write to CSV ---
    with open(output_file, 'w', newline='') as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=['Id', 'firstName', 'lastName', 'salary', 'managerId'])
        writer.writeheader()
        for emp in employees:
            writer.writerow(emp)

    print(f"Mock data with {num_rows} rows written to {output_file}")

# Example usage:
generate_employee_data(15, 'mock_employees.csv')
