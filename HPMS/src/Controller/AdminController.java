package Controller;

import Model.Admin;
import Service.AdminService;

public class AdminController {
    private AdminService adminService;

    public AdminController() { this.adminService = null; }

    public AdminController(AdminService service) {
        this.adminService = service;
    }

    public void addAdmin(Admin admin) {
        if (adminService == null) throw new IllegalStateException("AdminService not initialized");
        adminService.save(admin);
    }

    public Admin getAdminById(int id) {
        if (adminService == null) throw new IllegalStateException("AdminService not initialized");
        return adminService.findById(id);
    }

    public void updateAdmin(Admin admin) {
        if (adminService == null) throw new IllegalStateException("AdminService not initialized");
        adminService.update(admin);
    }

    public void deleteAdmin(int id) {
        if (adminService == null) throw new IllegalStateException("AdminService not initialized");
        adminService.delete(id);
    }
}
