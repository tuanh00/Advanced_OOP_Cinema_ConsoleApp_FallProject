package User.Employee;

import CinemaManager.ComplaintManager;
import User.Client.Complaint;

public class SalerHandler extends EmployeeHandler{
    private String employeeId;

    public SalerHandler(String employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public void handleComplaint(Complaint complaint) {
        if (complaint.getSeverityLevel() == 1) {

            ComplaintManager complaintManager = new ComplaintManager();
            complaintManager.resolveComplaint(complaint, "Handled by Saler", this.employeeId);
        } else if (successor != null) {
            successor.handleComplaint(complaint);
        }
    }
}
