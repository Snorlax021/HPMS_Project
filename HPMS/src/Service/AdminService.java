package Service;

import Model.Admin;

public interface AdminService {
    void save(Admin admin);
    Admin findById(int id);
    void update(Admin admin);
    void delete(int id);
}
