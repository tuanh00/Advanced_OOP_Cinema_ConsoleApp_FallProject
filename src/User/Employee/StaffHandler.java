// StaffHandler.java
package User.Employee;

import CinemaManager.ComplaintManager;
import User.Client.Complaint;

public class StaffHandler extends EmployeeHandler {
    private String employeeId;

    public StaffHandler(String employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public void handleComplaint(Complaint complaint) {
        if (complaint.getSeverityLevel() == 2) {
            ComplaintManager complaintManager = new ComplaintManager();
            complaintManager.resolveComplaint(complaint, "Handled by Manager", this.employeeId);

        } else if (successor != null) {
            successor.handleComplaint(complaint);
        }
    }
}
