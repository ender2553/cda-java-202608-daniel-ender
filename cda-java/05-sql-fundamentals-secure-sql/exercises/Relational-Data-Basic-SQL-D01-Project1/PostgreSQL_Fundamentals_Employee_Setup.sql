-- =============================================================
-- PostgreSQL Fundamentals Exercise - Employee Setup Script
-- Purpose: Create and populate the employee table used in Part 1.
-- Naming convention: lowercase, snake_case, singular
-- =============================================================

-- Allows the script to be rerun during practice.
DROP TABLE IF EXISTS employee;

CREATE TABLE employee
(
    employee_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    job_title VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    salary NUMERIC(10, 2) NOT NULL,
    hire_date DATE NOT NULL,
    employment_status VARCHAR(20) NOT NULL
);

INSERT INTO employee
(
    first_name,
    last_name,
    job_title,
    department,
    salary,
    hire_date,
    employment_status
)
VALUES
('Tony', 'Stark', 'Senior Developer', 'technology', 125000.00, '2021-03-15', 'active'),
('Peter', 'Parker', 'Junior Developer', 'technology', 72000.00, '2025-06-10', 'active'),
('Diana', 'Prince', 'Security Analyst', 'cybersecurity', 98000.00, '2022-09-01', 'active'),
('Bruce', 'Wayne', 'Security Engineer', 'cybersecurity', 115000.00, '2020-01-20', 'active'),
('Clark', 'Kent', 'Business Analyst', 'operations', 85000.00, '2023-04-11', 'active'),
('Natasha', 'Romanoff', 'Project Manager', 'operations', 105000.00, '2021-11-08', 'active'),
('Steve', 'Rogers', 'Support Specialist', 'support', 65000.00, '2024-02-14', 'active'),
('Shuri', 'Udaku', 'Software Architect', 'technology', 140000.00, '2019-07-22', 'active'),
('Wanda', 'Maximoff', 'Data Analyst', 'data', 90000.00, '2023-08-17', 'active'),
('Sam', 'Wilson', 'Support Specialist', 'support', 63000.00, '2025-01-05', 'active'),
('Carol', 'Danvers', 'Security Manager', 'cybersecurity', 130000.00, '2018-10-29', 'active'),
('Scott', 'Lang', 'Junior Developer', 'technology', 70000.00, '2024-09-12', 'inactive');

-- Optional verification query. This is not one of the student exercises.
SELECT *
FROM employee;
