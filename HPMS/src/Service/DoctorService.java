package Service;

import Model.Doctor;

public interface DoctorService {
    void save(Doctor d);
    Doctor findById(int id);
    void update(Doctor d);
    void delete(int id);
}
