// EmployeeHandler.java
package User.Employee;

import User.Client.Complaint;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import CinemaManager.DatabaseConnection;

public abstract class EmployeeHandler {
    protected EmployeeHandler successor;

    public void setSuccessor(EmployeeHandler successor) {
        this.successor = successor;
    }

    public abstract void handleComplaint(Complaint complaint);

    protected void assignComplaint(Complaint complaint, String employeeId, String resolutionMessage) {
        // Update the complaint object
        complaint.setAssignedEmployeeId(employeeId);
        complaint.setResolution(resolutionMessage);
        complaint.setStatus("in progress");

        // Update the database
        String updateSql = "UPDATE Complaints SET assigned_employee_id = ?, resolution = ?, status = ? WHERE complaint_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setString(1, employeeId);
            pstmt.setString(2, resolutionMessage);
            pstmt.setString(3, complaint.getStatus());
            pstmt.setInt(4, complaint.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Complaint assigned successfully!");
            } else {
                System.out.println("Failed to assign complaint.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }
}
