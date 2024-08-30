package User.Employee;
import CinemaManager.DatabaseConnection;
import User.User;

import Enums.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Employee extends User{
    private static int employeeCounter = 1000;
    private Roles role;
    private int sales;
    public Employee(String id){this.setId(id);}
    public Employee() {
        this.setId();
    }

    // Getters and Setters
    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public int getSales() {
        return sales;
    }

    public void increaseSales(int newSales) {
        String sql = "UPDATE Employees SET sales = sales + ? WHERE employee_id = ?;";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, newSales);
            pstmt.setString(2, this.getId());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                this.sales += newSales;
                System.out.println("Sales updated successfully for employee: " + this.getId());
            } else {
                System.out.println("No rows affected, check if employee ID: " + this.getId() + " exists.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating sales for employee ID " + this.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setId(){
        this.id = "EMP" + employeeCounter;
        ++employeeCounter;
    }

    @Override
    protected void display() {
        System.out.println("Employee ID: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Birthdate: " + getBirthdate());
        System.out.println("Role: " + getRole().toString());
        System.out.println("Sales: " + getSales());
    }

}
