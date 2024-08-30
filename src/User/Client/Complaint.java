package User.Client;

import Enums.Roles;
import User.Employee.Employee;

public class Complaint {
    private int id;
    private String customerId;
    private String description;
    private int severityLevel; // 1 for Saler, 2 for Staff, 3 for Manager
    private String status;
    private String assignedEmployeeId;
    private String resolution;

    public Complaint(String customerId, String description, int severityLevel) {
        this.customerId = customerId;
        this.description = description;
        this.severityLevel = severityLevel;
        this.status = "open"; // Default status when a new complaint is created
        // initially no employee is assigned and no resolution is provided
        this.assignedEmployeeId = null;
        this.resolution = null;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    public String getCustomerId() { return customerId; }
    public String getDescription() { return description; }
    public int getSeverityLevel() { return severityLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getAssignedEmployeeId() { return assignedEmployeeId; }
    public void setAssignedEmployeeId(String assignedEmployeeId) { this.assignedEmployeeId = assignedEmployeeId; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
