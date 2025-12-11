package Service;

import Model.Staff;

public interface StaffService {
    void save(Staff s);
    Staff findById(int id);
    void update(Staff s);
    void delete(int id);
}
