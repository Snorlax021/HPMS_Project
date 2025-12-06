-- HPMS Initial Data
-- Sample data for development and testing

-- Insert default users
INSERT INTO users (id, username, password_hash, role, created_at) VALUES
    ('user-1', 'admin', '$2a$10$xN3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'ADMIN', CURRENT_TIMESTAMP),
    ('user-2', 'doctor', '$2a$10$xN3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'DOCTOR', CURRENT_TIMESTAMP),
    ('user-3', 'staff', '$2a$10$xN3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'STAFF', CURRENT_TIMESTAMP),
    ('user-4', 'patient', '$2a$10$xN3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'PATIENT', CURRENT_TIMESTAMP);
-- Note: All passwords are hashed with BCrypt. Default password for all users: 'admin123'

-- Insert sample departments
INSERT INTO departments (id, name, description, created_at) VALUES
    ('dept-1', 'Cardiology', 'Heart and cardiovascular system', CURRENT_TIMESTAMP),
    ('dept-2', 'Neurology', 'Brain and nervous system', CURRENT_TIMESTAMP),
    ('dept-3', 'Orthopedics', 'Bones, joints, and muscles', CURRENT_TIMESTAMP),
    ('dept-4', 'Pediatrics', 'Medical care for infants, children, and adolescents', CURRENT_TIMESTAMP),
    ('dept-5', 'Emergency', 'Emergency medical services', CURRENT_TIMESTAMP);

-- Insert sample doctors
INSERT INTO doctors (id, first_name, last_name, specialization, license_number, phone, email, created_at) VALUES
    ('doc-1', 'John', 'Smith', 'Cardiology', 'LIC-001', '5551234567', 'john.smith@hpms.com', CURRENT_TIMESTAMP),
    ('doc-2', 'Sarah', 'Johnson', 'Neurology', 'LIC-002', '5551234568', 'sarah.johnson@hpms.com', CURRENT_TIMESTAMP),
    ('doc-3', 'Michael', 'Williams', 'Orthopedics', 'LIC-003', '5551234569', 'michael.williams@hpms.com', CURRENT_TIMESTAMP),
    ('doc-4', 'Emily', 'Brown', 'Pediatrics', 'LIC-004', '5551234570', 'emily.brown@hpms.com', CURRENT_TIMESTAMP);

-- Insert sample patients
INSERT INTO patients (id, first_name, last_name, date_of_birth, gender, phone, email, address, created_at) VALUES
    ('pat-1', 'Jane', 'Doe', '1990-01-15', 'Female', '5559876543', 'jane.doe@example.com', '123 Main St, City, State 12345', CURRENT_TIMESTAMP),
    ('pat-2', 'Robert', 'Wilson', '1985-05-22', 'Male', '5559876544', 'robert.wilson@example.com', '456 Oak Ave, City, State 12345', CURRENT_TIMESTAMP),
    ('pat-3', 'Mary', 'Davis', '1978-08-30', 'Female', '5559876545', 'mary.davis@example.com', '789 Pine Rd, City, State 12345', CURRENT_TIMESTAMP),
    ('pat-4', 'James', 'Miller', '1995-12-10', 'Male', '5559876546', 'james.miller@example.com', '321 Elm St, City, State 12345', CURRENT_TIMESTAMP);

-- Insert sample appointments
INSERT INTO appointments (id, patient_id, staff_id, scheduled_at, reason, status, created_at) VALUES
    ('appt-1', 'pat-1', 'doc-1', DATEADD('DAY', 7, CURRENT_TIMESTAMP), 'Annual checkup', 'SCHEDULED', CURRENT_TIMESTAMP),
    ('appt-2', 'pat-2', 'doc-2', DATEADD('DAY', 5, CURRENT_TIMESTAMP), 'Follow-up consultation', 'SCHEDULED', CURRENT_TIMESTAMP),
    ('appt-3', 'pat-3', 'doc-3', DATEADD('DAY', 3, CURRENT_TIMESTAMP), 'Knee pain evaluation', 'SCHEDULED', CURRENT_TIMESTAMP),
    ('appt-4', 'pat-4', 'doc-4', DATEADD('DAY', 1, CURRENT_TIMESTAMP), 'Vaccination', 'SCHEDULED', CURRENT_TIMESTAMP);

-- Insert sample visits (past visits)
INSERT INTO visits (id, patient_id, doctor_id, visit_date, diagnosis, prescription, notes, created_at) VALUES
    ('visit-1', 'pat-1', 'doc-1', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'Hypertension', 'Lisinopril 10mg once daily', 'Blood pressure monitored. Follow-up in 3 months.', CURRENT_TIMESTAMP),
    ('visit-2', 'pat-2', 'doc-2', DATEADD('DAY', -15, CURRENT_TIMESTAMP), 'Migraine', 'Sumatriptan 50mg as needed', 'Advised to keep headache diary.', CURRENT_TIMESTAMP),
    ('visit-3', 'pat-3', 'doc-3', DATEADD('DAY', -7, CURRENT_TIMESTAMP), 'Sprained ankle', 'Ibuprofen 400mg TID', 'Rest and ice. Physical therapy recommended.', CURRENT_TIMESTAMP);

-- Insert sample billing records
INSERT INTO billing (id, patient_id, amount, description, status, created_at, paid_at) VALUES
    ('bill-1', 'pat-1', 150.00, 'Consultation fee', 'PAID', DATEADD('DAY', -30, CURRENT_TIMESTAMP), DATEADD('DAY', -25, CURRENT_TIMESTAMP)),
    ('bill-2', 'pat-2', 200.00, 'Neurological examination', 'PAID', DATEADD('DAY', -15, CURRENT_TIMESTAMP), DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
    ('bill-3', 'pat-3', 175.00, 'X-ray and consultation', 'UNPAID', DATEADD('DAY', -7, CURRENT_TIMESTAMP), NULL),
    ('bill-4', 'pat-4', 50.00, 'Vaccination fee', 'UNPAID', CURRENT_TIMESTAMP, NULL);

-- Insert sample insurance policies
INSERT INTO insurance_policies (id, patient_id, provider, policy_number, coverage_amount, start_date, end_date, created_at) VALUES
    ('ins-1', 'pat-1', 'HealthFirst Insurance', 'POL-001-2024', 500000.00, '2024-01-01', '2024-12-31', CURRENT_TIMESTAMP),
    ('ins-2', 'pat-2', 'MediCare Plus', 'POL-002-2024', 750000.00, '2024-01-01', '2024-12-31', CURRENT_TIMESTAMP),
    ('ins-3', 'pat-3', 'SecureHealth', 'POL-003-2024', 1000000.00, '2024-01-01', '2024-12-31', CURRENT_TIMESTAMP);
