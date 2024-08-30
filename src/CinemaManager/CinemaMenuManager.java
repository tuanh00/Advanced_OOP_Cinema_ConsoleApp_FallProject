package CinemaManager;

import User.Employee.*;

import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CinemaMenuManager {
    private Scanner scanner;
    private ComplaintManager complaintManager;
    private DatabaseInitializer dbInitializer;
    private SalerHandler salerHandler;
    private StaffHandler staffHandler;
    private ManagerHandler managerHandler;

    public CinemaMenuManager() {
        scanner = new Scanner(System.in);
        complaintManager = new ComplaintManager();
        dbInitializer = new DatabaseInitializer();
    }


    public void run() {
        this.displayMenu();
    }

    public void displayMenu(){
        System.out.println("\n********** Welcome to the Cinema System **********\n");
        while(true){
            System.out.println("\t1. Login as Employee");
            System.out.println("\t2. Login as Customer");
            System.out.println("\t3. Exit");
            System.out.println("\nPlease choose an option:");

            try {
                int entryOption = this.scanner.nextInt();
                this.scanner.nextLine();
                switch (entryOption) {
                    case 1:
                        if (this.dbInitializer.performEmployeeLogin()) {
                            this.employeeMenu();
                        }
                        break;
                    case 2:
                        if (this.dbInitializer.performCustomerLogin()) {
                            this.customerMenu();
                        }
                        break;
                    case 3:
                        System.out.println("\nExiting the system... Goodbye!");
                        return;
                    default:
                        System.err.println("Invalid option. Please try again.\n");
                }
            } catch (InputMismatchException var2) {
                System.err.println("Invalid input. Please enter a number.\n");
                this.scanner.nextLine();
            }
        }
    }

    private void employeeMenu() {
        boolean isManager = "MANAGER".equals(this.dbInitializer.getCurrentEmployeeRole());
        System.out.println("\n********** Employee Menu **********");

        while (true) {
            System.out.println("1. Display All Movies");
            System.out.println("2. Search for a Movie");
            System.out.println("3. Display All Advertisements");
            System.out.println("4. Search for an Advertisement");
            System.out.println("5. Display All Customers");
            System.out.println("6. Add New Customer");
            System.out.println("7. Reserve a Movie");
            System.out.println("8. Cancel a Reservation");
            System.out.println("9. Search for a Customer");
            System.out.println("10. View complaints");
            if (isManager) {
                System.out.println("11. Add New Movie");
                System.out.println("12. Add New Advertisement");
                System.out.println("13. Add New Employee");
                System.out.println("14. Remove a Movie");
                System.out.println("15. Remove an Advertisement");
                System.out.println("16. Display All Employees");
            }

            System.out.println("17. Logout");
            System.out.print("\nPlease choose an option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline left-over

                switch (choice) {
                    case 1:
                        dbInitializer.displayAllMovies(); // Assuming dbInitializer has this method implemented
                        break;
                    case 2:
                        dbInitializer.searchForMovie();
                        break;
                    case 3:
                        dbInitializer.displayAllAdvertisements(); // Assuming dbInitializer has this method implemented
                        break;
                    case 4:
                        dbInitializer.searchForAdvertisement();
                        break;
                    case 5:
                        dbInitializer.displayAllCustomers(); // Assuming dbInitializer has this method implemented
                        break;
                    case 6:
                        dbInitializer.addNewCustomer();
                        break;
                    case 7:
                            dbInitializer.reserveMovieForCustomers();
                            break;
                    case 8:
                        dbInitializer.cancelReservationByEmployee();
                        break;
                    case 9:
                        dbInitializer.searchForCustomer();
                        break;
                    case 10:
                        dbInitializer.handleAndResolveComplaint();
                        break;
                    case 11:
                        if (isManager) {
                            dbInitializer.addNewMovie();
                        } else {
                            System.out.println("Unauthorized action. This option is only for managers.");
                        }
                        break;
                    case 12:
                        if (isManager) {
                            dbInitializer.addNewAdvertisement();
                        } else {
                            System.out.println("Unauthorized action. This option is only for managers.");
                        }
                        break;
                    case 13:
                        if (isManager) {
                            dbInitializer.addNewEmployee();
                        } else {
                            System.out.println("Unauthorized action. This option is only for managers.");
                        }
                        break;
                    case 14:
                        if (isManager) {
                            dbInitializer.removeMovie();
                        } else {
                            System.out.println("Unauthorized action. This option is only for managers.");
                        }
                        break;
                    case 15:
                        if (isManager) {
                            dbInitializer.removeAdvertisement();
                        } else {
                            System.out.println("Unauthorized action. This option is only for managers.");
                        }
                        break;
                    case 16:
                        if (isManager) {
                            dbInitializer.displayAllEmployees();
                        } else {
                            System.out.println("Unauthorized action. This option is only for managers.");
                        }
                        break;

                    case 17:
                        System.out.println("Logging out as employee...\n");
                        return;

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (InputMismatchException var4) {
                System.err.println("Invalid input. Please enter a number.");
                this.scanner.nextLine();
            }
            System.out.println("Press enter to continue");
            this.scanner.nextLine();

        }
    }

    private void customerMenu() {
        System.out.println("\n********** Customer Menu **********");

        while (true) {
            System.out.println("1. Display All Movies");
            System.out.println("2. Search for a Movie");
            System.out.println("3. Reserve a Movie");
            System.out.println("4. Cancel a Reservation");
            System.out.println("5. Send a Complaint");
            System.out.println("6. Logout");
            System.out.print("\nPlease choose an option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline left-over

                switch (choice) {
                    case 1:
                        dbInitializer.displayMoviesBasedOnAge(); // This method displays all movies
                        break;
                    case 2:
                        dbInitializer.searchForMovie(); // This method searches movies based on criteria
                        break;
                    case 3:
                        dbInitializer.reserveMovie(); // This method handles movie reservation
                        break;
                    case 4:
                        dbInitializer.cancelReservation(); // This method handles reservation cancellation
                        break;
                    case 5:
                        dbInitializer.sendComplaint();
                        break;
                    case 6:
                        System.out.println("Logging out as customer...");
                        return;

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (InputMismatchException var3) {
                System.err.println("Invalid input. Please enter a number.");
                this.scanner.nextLine();
            }
            System.out.println("Press enter to continue");
            this.scanner.nextLine();
        }
    }
}

