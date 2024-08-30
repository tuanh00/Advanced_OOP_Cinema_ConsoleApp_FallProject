package User.Client;

import CinemaManager.DatabaseConnection;
import Enums.PaymentMethod;
import User.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Customer extends User{
    private static int customerCounter = 1000;
    private int points;
    private PaymentMethod paymentMethod;
    public Customer(){}
    public Customer(String name, String birthdate, PaymentMethod payment_method, String password){
        this.setId();
        this.setName(name);
        this.setBirthdate(birthdate);
        this.points = 100; // Initialize points to 100 for new customers
        this.paymentMethod = paymentMethod;
        this.setPassword(password);
    }

    public void increasePoints(int newPoints) {
        String sql = "UPDATE Customers SET points = points + ? WHERE customer_id = ?;";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newPoints);
            pstmt.setString(2, this.getId());
            pstmt.executeUpdate();

            // Update local customer object points
            this.setPoints(this.getPoints() + newPoints);

        } catch (SQLException e) {
            System.out.println("Error updating points for customer ID " + this.getId() + ": " + e.getMessage());
        }
    }
    // Getters and Setters
    public int getPoints() {
        return points;
    }
    public void setPoints(int points) {
        this.points = points;
    }
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public void setId(){
        this.id = "CST" + customerCounter;
        ++customerCounter;
    }
    public void setId(String id) {
        this.id = id;
    }

    public static synchronized String getNextCustomerId() {
        return "CST" + customerCounter++;
    }

    @Override
    protected void display() {
        // Implement display logic for customer details
        System.out.println("Customer ID: " + getId());
        System.out.println("Name: " + getName());
        System.out.println("Birthdate: " + getBirthdate());
        System.out.println("Points: " + getPoints());
        System.out.println("Payment Method: " + getPaymentMethod());
    }
}
