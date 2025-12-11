package Controller;

import Model.Staff;
import Service.StaffService;

public class StaffController {
    private StaffService staffService;

    public StaffController(StaffService service) {
        this.staffService = service;
    }

    public void addStaff(Staff staff) {
        staffService.save(staff);
    }

    public Staff getStaffById(int id) {
        return staffService.findById(id);
    }

    public void updateStaff(Staff staff) {
        staffService.update(staff);
    }

    public void deleteStaff(int id) {
        staffService.delete(id);
    }
}
