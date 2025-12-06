-- HPMS Database Schema
-- Tables for Hospital Patient Management System

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_username ON users(username);

-- Patients table
CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_patient_name ON patients(last_name, first_name);
CREATE INDEX IF NOT EXISTS idx_patient_email ON patients(email);

-- Doctors table
CREATE TABLE IF NOT EXISTS doctors (
    id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    license_number VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_doctor_specialization ON doctors(specialization);

-- Departments table
CREATE TABLE IF NOT EXISTS departments (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id VARCHAR(255) PRIMARY KEY,
    patient_id VARCHAR(255) NOT NULL,
    staff_id VARCHAR(255),
    scheduled_at TIMESTAMP NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_appointment_patient ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointment_scheduled ON appointments(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_appointment_status ON appointments(status);

-- Visits table
CREATE TABLE IF NOT EXISTS visits (
    id VARCHAR(255) PRIMARY KEY,
    patient_id VARCHAR(255) NOT NULL,
    doctor_id VARCHAR(255),
    visit_date TIMESTAMP NOT NULL,
    diagnosis VARCHAR(1000),
    prescription VARCHAR(1000),
    notes VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_visit_patient ON visits(patient_id);
CREATE INDEX IF NOT EXISTS idx_visit_date ON visits(visit_date);

-- Billing table
CREATE TABLE IF NOT EXISTS billing (
    id VARCHAR(255) PRIMARY KEY,
    patient_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_billing_patient ON billing(patient_id);
CREATE INDEX IF NOT EXISTS idx_billing_status ON billing(status);

-- Insurance Policies table
CREATE TABLE IF NOT EXISTS insurance_policies (
    id VARCHAR(255) PRIMARY KEY,
    patient_id VARCHAR(255) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    policy_number VARCHAR(100) NOT NULL,
    coverage_amount DECIMAL(12, 2),
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_insurance_patient ON insurance_policies(patient_id);
CREATE INDEX IF NOT EXISTS idx_insurance_policy_number ON insurance_policies(policy_number);
