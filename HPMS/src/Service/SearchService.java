package Service;

import Model.Patient;
import Model.Doctor;
import java.util.List;

public interface SearchService {
    List<Patient> findPatients(String keyword);
    List<Doctor> findDoctors(String keyword);
}
