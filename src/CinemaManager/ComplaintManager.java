package CinemaManager;

import User.Client.Complaint;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ComplaintManager {

    /**
     * Resolves a complaint by updating its status in the database.
     *
     * @param complaint The complaint to be resolved.
     * @param resolutionText The resolution details to be saved.
     * @param employeeId The ID of the employee resolving the complaint.
     */
    public void resolveComplaint(Complaint complaint, String resolutionText, String employeeId) {
        // SQL statement to update the complaint record
        String updateSql = "UPDATE Complaints SET status = 'resolved', resolution = ?, assigned_employee_id = ? WHERE complaint_id = ?";

        // Attempt to connect to the database and update the complaint
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            // Set the prepared statement parameters
            pstmt.setString(1, resolutionText);
            pstmt.setString(2, employeeId);
            pstmt.setInt(3, complaint.getId());

            // Execute the update
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Complaint resolved successfully!");
                // Update the complaint object as well
                complaint.setStatus("resolved");
                complaint.setResolution(resolutionText);
                complaint.setAssignedEmployeeId(employeeId);
            } else {
                System.out.println("Failed to resolve complaint. Complaint may already have been resolved or does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception while resolving complaint: " + e.getMessage());
        }
    }
}
