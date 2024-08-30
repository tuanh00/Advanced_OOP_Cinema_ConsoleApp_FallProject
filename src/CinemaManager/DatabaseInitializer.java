package CinemaManager;

import Enums.Language;
import Enums.PaymentMethod;
import Factory.AbstractFactory;
import MediaContent.Advertisement.Advertisement;
import MediaContent.Movie.Movie;
import User.Client.Complaint;
import User.Client.Customer;
import User.Employee.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class DatabaseInitializer {
    private Scanner scanner;
    private String currentUserID;
    private EmployeeHandler complaintChainHead;

    public DatabaseInitializer(){
        this.scanner = new Scanner(System.in);
        this.createNewTables();
        this.setupComplaintHandlingChain();
    }

    private void createMoviesTable() {
        executeSql("CREATE TABLE IF NOT EXISTS Movies (" +
                "movie_id TEXT PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "duration INTEGER NOT NULL," +
                "genre TEXT NOT NULL," +
                "age_restriction INTEGER DEFAULT 0," +
                "price REAL NOT NULL," +
                "subtitles TEXT);");
    }
    private void createAdvertisementsTable() {
        executeSql("CREATE TABLE IF NOT EXISTS Advertisements (" +
                "ad_id TEXT PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "duration INTEGER NOT NULL," +
                "age_restriction INTEGER DEFAULT 0," +
                "subtitles TEXT);");
    }
    private void createEmployeesTable(){
        executeSql("CREATE TABLE IF NOT EXISTS Employees (" +
                "employee_id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "date_of_birth TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "sales INTEGER NOT NULL DEFAULT 0," +
                "password TEXT NOT NULL);");
    }
    private void createCustomersTable(){
        executeSql("CREATE TABLE IF NOT EXISTS Customers (" +
                "customer_id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "date_of_birth TEXT NOT NULL," +
                "points INTEGER NOT NULL DEFAULT 0," +
                "payment_method TEXT NOT NULL," +
                "password TEXT NOT NULL);");
    }
    private void createCinemaHallsTable(){
        executeSql("CREATE TABLE IF NOT EXISTS CinemaHalls (" +
                "hall_id TEXT PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "capacity INTEGER NOT NULL," +
                "rows INTEGER NOT NULL," +
                "seats_per_row INTEGER NOT NULL);");
    }
    private void createSeatsTable() {
        executeSql("CREATE TABLE IF NOT EXISTS Seats (" +
                "seat_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "hall_id TEXT NOT NULL," +
                "showtime_id INTEGER," +
                "row CHAR(1)," +
                "seat_number INTEGER," +
                "status TEXT CHECK(status IN('available', 'reserved')) NOT NULL DEFAULT 'available'," +
                "reservation_id INTEGER," + // Added line
                "FOREIGN KEY(hall_id) REFERENCES CinemaHalls(hall_id)," +
                "FOREIGN KEY(showtime_id) REFERENCES Showtimes(showtime_id)," +
                "FOREIGN KEY(reservation_id) REFERENCES Reservations(reservation_id));" // Added line
        );
    }
    private void createShowtimesTable(){
        executeSql("CREATE TABLE IF NOT EXISTS Showtimes (" +
                "showtime_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "movie_id TEXT," +
                "hall_id TEXT," +
                "start_time TEXT NOT NULL," +
                "end_time TEXT NOT NULL," +
                "FOREIGN KEY(movie_id) REFERENCES Movies(movie_id)," +
                "FOREIGN KEY(hall_id) REFERENCES CinemaHalls(hall_id));"
        );
    }
    private void createReservationsTable(){
        executeSql("CREATE TABLE IF NOT EXISTS Reservations (" +
                "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_id TEXT," +
                "showtime_id INTEGER," +
                "employee_id TEXT," + // Keep track who helps customer reserve the ticket will get increment in sales
                "num_seats INTEGER NOT NULL," +
                "status TEXT CHECK(status IN ('confirmed', 'cancelled')) NOT NULL," +
                "FOREIGN KEY(customer_id) REFERENCES Customers(customer_id)," +
                "FOREIGN KEY(showtime_id) REFERENCES Showtimes(showtime_id)," +
                "FOREIGN KEY(employee_id) REFERENCES Employees(employee_id));"
        );
    }
    private void createComplaintsTable(){
        executeSql("CREATE TABLE IF NOT EXISTS Complaints (" +
                "complaint_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_id TEXT NOT NULL," +
                "description TEXT NOT NULL," +
                "severity_level INTEGER NOT NULL," +
                "status TEXT NOT NULL DEFAULT 'open'," +
                "assigned_employee_id TEXT," +
                "resolution TEXT," +
                "FOREIGN KEY(customer_id) REFERENCES Customers(customer_id)," +
                "FOREIGN KEY(assigned_employee_id) REFERENCES Employees(employee_id));"
        );
    }

    public void createNewTables() {
        createMoviesTable();
        createAdvertisementsTable();
        createEmployeesTable();
        createCustomersTable();
        createCinemaHallsTable();
        createSeatsTable();
        createShowtimesTable();
        createReservationsTable();
        createComplaintsTable();

        // Insert sample data into tables after they have been created
        insertSampleData();
    }
    private void executeSql(String sql) {
        Statement stmt = null;
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection(); // Get the connection
            stmt = conn.createStatement(); // Create a statement
            stmt.execute(sql); // Execute the SQL
            System.out.println("Table created successfully.");
        } catch (SQLException e) {
            System.out.println("An error occurred while creating table: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close(); // Close only the statement
            } catch (SQLException ex) {
                System.out.println("An error occurred while closing the statement: " + ex.getMessage());
            }
        }
    }


    public String getCurrentEmployeeRole() {
        if (this.currentUserID == null) {
            System.out.println("No user is currently logged in.");
            return null; // Ensuring there is a logged-in user
        }

        String query = "SELECT role FROM Employees WHERE employee_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Using the Singleton instance
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, this.currentUserID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching the employee role: " + e.getMessage());
        }
        return null; // Role not found or error
    }
    public boolean performEmployeeLogin() {
        System.out.println("Please enter your employee ID:");
        String employeeID = scanner.nextLine();
        System.out.println("Please enter your password:");
        String password = scanner.nextLine();

        String loginSql = "SELECT name FROM Employees WHERE employee_id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(loginSql)) {

            pstmt.setString(1, employeeID);
            pstmt.setString(2, password);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                // Login success
                String employeeName = resultSet.getString("name"); // Retrieve the employee's name
                this.currentUserID = employeeID; // Store the logged-in employee's ID
                System.out.println("Login successful as " + employeeName + "!");
                return true;
            } else {
                // Login fail
                System.out.println("Login failed. Incorrect employee ID or password.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("An error occurred during employee login: " + e.getMessage());
            return false;
        }
    }
    public boolean performCustomerLogin() {
        System.out.println("Please enter your customer ID:");
        String customerID = scanner.nextLine();
        System.out.println("Please enter your password:");
        String password = scanner.nextLine();

        String loginSql = "SELECT name FROM Customers WHERE customer_id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(loginSql)) {

            pstmt.setString(1, customerID);
            pstmt.setString(2, password);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                // Login success
                String customerName = resultSet.getString("name"); // Retrieve the customer's name
                this.currentUserID = customerID; // Store the logged-in customer's ID
                System.out.println("Login successful as " + customerName + "!");
                return true;
            } else {
                // Login fail
                System.out.println("Login failed. Incorrect customer ID or password.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("An error occurred during customer login: " + e.getMessage());
            return false;
        }
    }
    public void insertSampleData() {
        // Insert sample data into the Movies table
        insertSampleMovies();

        // Insert sample data into the Advertisements table
        insertSampleAdvertisements();

        // Insert sample data into the Employees table
        insertSampleEmployees();

        // Insert sample data into the Customers table
        insertSampleCustomers();

        // Insert sample data into the CinemaHalls table
        insertSampleCinemaHalls();

        // Insert sample data into the Showtimes table
        insertSampleShowtimes();

        // Insert sample data into the Reservations table
        insertSampleReservations();

        // Insert sample data into Seats table
        insertSampleSeats();

        // Insert sample data into Complaints table
        insertSampleComplaints();
    }

    public void insertMovie(Movie movie) {
        String checkSql = "SELECT count(*) FROM Movies WHERE movie_id = ?";
        String insertSql = "INSERT INTO Movies (movie_id, title, duration, genre, age_restriction, price, subtitles) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Check for existing movie ID
            checkStmt.setString(1, movie.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Movie with ID " + movie.getId() + " already exists. Skipping insertion.");
                return;
            }

            // Proceed with insertion if no duplicate found
            insertStmt.setString(1, movie.getId());
            insertStmt.setString(2, movie.getTitle());
            insertStmt.setInt(3, movie.getDuration());
            insertStmt.setString(4, movie.getGenre());
            insertStmt.setInt(5, movie.getAgeRestriction());
            insertStmt.setDouble(6, movie.getPrice());
            // Convert the Language enum to String
            insertStmt.setString(7, movie.getSubtitles().name());

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Movie inserted successfully!");
            } else {
                System.out.println("Failed to insert movie.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }

    public void insertSampleMovies() {
        List<Movie> movies = new ArrayList<>();
        Movie m1 = AbstractFactory.factory(Language.EN).createMovie("Inception", 148, "Sci-fi", 12, 99.99);
        movies.add(m1);
        Movie m2 = AbstractFactory.factory(Language.EN).createMovie("The Matrix", 136, "Sci-fi", 15, 89.99);
        movies.add(m2);
        movies.add(AbstractFactory.factory(Language.EN).createMovie("Eternal Sunshine", 108, "Drama", 16, 59.99));
        movies.add(AbstractFactory.factory(Language.EN).createMovie("Gravity", 91, "Sci-Fi", 12, 69.99));

        movies.add(AbstractFactory.factory(Language.FR).createMovie("Le Voyage dans la Lune", 18, "Sci-fi", 0, 19.99));
        movies.add(AbstractFactory.factory(Language.FR).createMovie("Amélie", 122, "Action", 12, 79.99));
        movies.add(AbstractFactory.factory(Language.FR).createMovie("Eternal Sunshine", 108, "Drama", 16, 59.99));
        movies.add(AbstractFactory.factory(Language.FR).createMovie("Gravity", 91, "Sci-Fi", 12, 69.99));


        Iterator var12 = movies.iterator();

        while(var12.hasNext()){
            Movie movie = (Movie) var12.next();
            this.insertMovie(movie);
        }

    }

    public void insertAdvertisement(Advertisement advertisement) {
        String checkSql = "SELECT count(*) FROM Advertisements WHERE ad_id = ?";
        String insertSql = "INSERT INTO Advertisements (ad_id, title, duration, age_restriction, subtitles) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Check for existing advertisement ID
            checkStmt.setString(1, advertisement.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Advertisement with ID " + advertisement.getId() + " already exists. Skipping insertion.");
                return;
            }

            // Proceed with insertion if no duplicate found
            insertStmt.setString(1, advertisement.getId());
            insertStmt.setString(2, advertisement.getTitle());
            insertStmt.setInt(3, advertisement.getDuration());
            insertStmt.setInt(4, advertisement.getAgeRestriction());
            insertStmt.setString(5, advertisement.getSubtitles().name()); // Assuming you have a method similar to movies to get subtitles as Language

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Advertisement inserted successfully!");
            } else {
                System.out.println("Failed to insert advertisement.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }
    public void insertSampleAdvertisements() {
        List<Advertisement> advertisements = new ArrayList<>();
        advertisements.add(AbstractFactory.factory(Language.EN).createAdvertisement("Summer Sale",3,0));
        advertisements.add(AbstractFactory.factory(Language.EN).createAdvertisement("Fall Sale",3,0));
        advertisements.add(AbstractFactory.factory(Language.FR).createAdvertisement("Winter Sale",2,0));
        advertisements.add(AbstractFactory.factory(Language.FR).createAdvertisement("Spring Sale",4,0));

        Iterator var12 = advertisements.iterator();
        while(var12.hasNext()){
            Advertisement advertisement = (Advertisement) var12.next();
            this.insertAdvertisement(advertisement);
        }
    }

    public void insertEmployee(Employee employee) {
        String checkDuplicateSql = "SELECT COUNT(*) AS count FROM Employees WHERE employee_id = ?";
        String insertSql = "INSERT INTO Employees (employee_id, name, date_of_birth, role, sales, password) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateSql);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            // Check for duplicate employee ID
            checkStmt.setString(1, employee.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("An employee with ID " + employee.getId() + " already exists.");
                return;
            }

            // Insert the new employee since no duplicate was found
            pstmt.setString(1, employee.getId());
            pstmt.setString(2, employee.getName());
            pstmt.setString(3, employee.getBirthdate());
            pstmt.setString(4, employee.getRole().toString());
            pstmt.setInt(5, employee.getSales());
            pstmt.setString(6, employee.getPassword());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Employee inserted successfully!");
            } else {
                System.out.println("Failed to insert employee data.");
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while inserting employee: " + e.getMessage());
        }
    }

    public void insertSampleEmployees() {
        /*
          // Employees table
                "CREATE TABLE IF NOT EXISTS Employees (" +
                        "employee_id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "date_of_birth TEXT NOT NULL," +
                        "role TEXT NOT NULL," +
                        "sales INTEGER NOT NULL DEFAULT 0," +
                        "password TEXT NOT NULL);",

        * */
        List<Employee> employees = new ArrayList();
        EmployeeMaker salerMaker = new EmployeeMaker(new SalerEmpBuilder());
        EmployeeMaker staffMaker = new EmployeeMaker(new StaffEmpBuilder());
        EmployeeMaker mgrMaker = new EmployeeMaker(new ManagerEmpBuilder());

        salerMaker.makeEmployee("Tu Anh", "06-05-2001", "123");
        Employee emp1 = salerMaker.getEmployee();
        salerMaker.reset(new SalerEmpBuilder());
        employees.add(emp1);

        salerMaker.makeEmployee("Tiffany", "06-13-2000","123");
        Employee emp2 = salerMaker.getEmployee();
        salerMaker.reset(new SalerEmpBuilder());
        employees.add(emp2);

        salerMaker.makeEmployee("Alice", "08-20-2000", "123");
        Employee emp3 = salerMaker.getEmployee();
        salerMaker.reset(new SalerEmpBuilder());
        employees.add(emp3);

        salerMaker.makeEmployee("Sophie", "05-21-1997", "pwd123");
        Employee emp4 = salerMaker.getEmployee();
        salerMaker.reset(new SalerEmpBuilder());
        employees.add(emp4);

        staffMaker.makeEmployee("John", "06-06-1999", "secret123");
        Employee emp5 = staffMaker.getEmployee();
        staffMaker.reset(new StaffEmpBuilder());
        employees.add(emp5);

        staffMaker.makeEmployee("Isabelle", "07-08-1990", "secret123");
        Employee emp6 = staffMaker.getEmployee();
        staffMaker.reset(new StaffEmpBuilder());
        employees.add(emp6);

        mgrMaker.makeEmployee("Emily", "04-25-1980","pwd123");
        Employee emp7 = mgrMaker.getEmployee();
        mgrMaker.reset(new ManagerEmpBuilder());
        employees.add(emp7);

        mgrMaker.makeEmployee("David","11-12-1975", "pwd123");
        Employee emp8 = mgrMaker.getEmployee();
        mgrMaker.reset(new ManagerEmpBuilder());
        employees.add(emp8);

        Iterator var12 = employees.iterator();
        while(var12.hasNext()){
            Employee employee = (Employee) var12.next();
            this.insertEmployee(employee);
        }

    }
    public void insertCustomer(Customer customer) {
        String checkDuplicateSql = "SELECT COUNT(*) AS count FROM Customers WHERE customer_id = ?";
        String insertSql = "INSERT INTO Customers (customer_id, name, date_of_birth, points, payment_method, password) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkDuplicateSql);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            // Check for duplicate customer ID
            checkStmt.setString(1, customer.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("A customer with ID " + customer.getId() + " already exists.");
                return;
            }

            // Insert the new customer since no duplicate was found
            pstmt.setString(1, customer.getId());
            pstmt.setString(2, customer.getName());
            pstmt.setString(3, customer.getBirthdate());
            pstmt.setInt(4, customer.getPoints());
            if (customer.getPaymentMethod() != null) {
                pstmt.setString(5, customer.getPaymentMethod().toString());
            } else {
                // Handle the case where getPaymentMethod() is null
                pstmt.setString(5, "default_payment_method"); // Or however you want to handle this case
            }            pstmt.setString(6, customer.getPassword());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Customer inserted successfully!");
            } else {
                System.out.println("Failed to insert customer data.");
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while inserting customer: " + e.getMessage());
        }
    }

    public void insertSampleCustomers() {
        List<Customer> customers = new ArrayList<>();
        Customer c1 = new Customer("Jones","10-10-1980", PaymentMethod.PAYPAL, "password");
        customers.add(c1);
        Customer c2 = new Customer("Daniella","12-15-1992", PaymentMethod.DEBIT, "pwd123");
        customers.add(c2);
        Customer c3 = new Customer("Emma","06-22-1970", PaymentMethod.DEBIT, "secure123");
        customers.add(c3);
        Customer c4 = new Customer("Tu","06-22-1970", PaymentMethod.CREDIT, "123");
        customers.add(c4);
        Customer c5 = new Customer("William","09-14-1987", PaymentMethod.PAYPAL, "williamPwd");
        customers.add(c5);
        Customer c6 = new Customer("Olivia","02-03-1995", PaymentMethod.PAYPAL, "oliviaPwd");
        customers.add(c6);
        Iterator var12 = customers.iterator();
        while(var12.hasNext()){
            Customer customer = (Customer) var12.next();
            this.insertCustomer(customer);
        }
    }

    public void insertSampleCinemaHalls() {
        // Define sample cinema halls
        String insertSql = "INSERT INTO CinemaHalls (hall_id, name, capacity, rows, seats_per_row) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            // Define details for 5 halls
            Object[][] hallDetails = {
                    {"HALL001", "Hall One", 100, 10, 10},
                    {"HALL002", "Hall Two", 120, 12, 10},
                    {"HALL003", "Hall Three", 150, 15, 10},
                    {"HALL004", "Hall Four", 200, 20, 10},
                    {"HALL005", "Hall Five", 180, 18, 10},
            };

            for (Object[] details : hallDetails) {
                pstmt.setString(1, (String) details[0]);
                pstmt.setString(2, (String) details[1]);
                pstmt.setInt(3, (Integer) details[2]);
                pstmt.setInt(4, (Integer) details[3]);
                pstmt.setInt(5, (Integer) details[4]);
                pstmt.executeUpdate();
            }

            System.out.println("Cinema halls inserted successfully.");
        } catch (SQLException e) {
            System.out.println("An error occurred while inserting cinema halls: " + e.getMessage());
        }
    }

    public void insertSampleShowtimes() {
        // SQL to get movies and their duration
        String moviesSql = "SELECT movie_id, duration FROM Movies";
        // SQL to get cinema halls
        String hallsSql = "SELECT hall_id FROM CinemaHalls";
        // SQL to insert showtimes
        String insertSql = "INSERT INTO Showtimes (movie_id, hall_id, start_time, end_time) VALUES (?, ?, ?, ?)";

        // This format assumes your database stores times as strings in the format "yyyy-MM-dd HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 14, 0); // Starting at 2 PM

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement moviesStmt = conn.createStatement();
             Statement hallsStmt = conn.createStatement();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            ResultSet moviesRs = moviesStmt.executeQuery(moviesSql);
            ResultSet hallsRs = hallsStmt.executeQuery(hallsSql);

            List<String> movieIds = new ArrayList<>();
            Map<String, Integer> movieDurations = new HashMap<>();

            // Collect all movies and their durations
            while (moviesRs.next()) {
                String movieId = moviesRs.getString("movie_id");
                int duration = moviesRs.getInt("duration");
                movieIds.add(movieId);
                movieDurations.put(movieId, duration);
            }

            // For each hall, schedule all movies sequentially for the day
            while (hallsRs.next()) {
                String hallId = hallsRs.getString("hall_id");
                LocalDateTime currentStartTime = startTime;

                for (String movieId : movieIds) {
                    // Calculate end time based on movie duration
                    int duration = movieDurations.get(movieId);
                    LocalDateTime endTime = currentStartTime.plusMinutes(duration);

                    // Prepare the insert statement
                    insertStmt.setString(1, movieId);
                    insertStmt.setString(2, hallId);
                    insertStmt.setString(3, formatter.format(currentStartTime));
                    insertStmt.setString(4, formatter.format(endTime));

                    // Insert the showtime
                    insertStmt.executeUpdate();

                    // Update start time for the next showtime, adding a buffer (e.g., 30 minutes for cleanup and preparation)
                    currentStartTime = endTime.plusMinutes(30);
                }
            }

            System.out.println("Sample showtimes inserted successfully.");
        } catch (SQLException e) {
            System.out.println("An error occurred while inserting showtimes: " + e.getMessage());
        }
    }
    public void insertSampleReservations() {
        // Define sample reservations
        String insertSql = "INSERT INTO Reservations (customer_id, showtime_id, employee_id, num_seats, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            // Insert reservation 1
            pstmt.setString(1, "CST1000"); // customer ID
            pstmt.setInt(2, 1); // showtime ID
            pstmt.setString(3, "EMP1000"); // employee ID who made the reservation
            pstmt.setInt(4, 4); // number of seats
            pstmt.setString(5, "confirmed"); // reservation status
            pstmt.executeUpdate();

            // ... Add more reservations as needed
            System.out.println("Reservations inserted successfully.");
        } catch (SQLException e) {
            System.out.println("An error occurred while inserting reservations: " + e.getMessage());
        }
    }
    public void insertSampleSeats() {
        List<String> hallIds = getAllHallIds();
        for (String hallId : hallIds) {
            insertSeatsForHall(hallId);
        }
    }
    private List<String> getAllHallIds() {
        List<String> hallIds = new ArrayList<>();
        String sql = "SELECT hall_id FROM CinemaHalls";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                hallIds.add(rs.getString("hall_id"));
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching hall IDs: " + e.getMessage());
        }
        return hallIds;
    }

    private void insertSeatsForHall(String hallId) {
        // Fetch hall layout details
        String hallDetailsSql = "SELECT rows, seats_per_row FROM CinemaHalls WHERE hall_id = ?";
        int rows = 0;
        int seatsPerRow = 0;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(hallDetailsSql)) {
            pstmt.setString(1, hallId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                rows = rs.getInt("rows");
                seatsPerRow = rs.getInt("seats_per_row");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching hall details: " + e.getMessage());
            return;
        }

        List<Integer> showtimeIds = getValidShowtimeIdsForHall(hallId);
        if (showtimeIds.isEmpty()) {
            System.out.println("No valid showtime IDs found for hall " + hallId);
            return;
        }

        String insertSql = "INSERT INTO Seats (hall_id, row, seat_number, status, showtime_id) VALUES (?, ?, ?, 'available', ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            for (Integer showtimeId : showtimeIds) {
                for (char row = 'A'; row < 'A' + rows; row++) {
                    for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                        pstmt.setString(1, hallId);
                        pstmt.setString(2, String.valueOf(row));
                        pstmt.setInt(3, seatNum);
                        pstmt.setInt(4, showtimeId);
                        pstmt.executeUpdate();
                    }
                }
            }

            System.out.println("Seats inserted for hall " + hallId);
        } catch (SQLException e) {
            System.out.println("An error occurred while inserting seats for hall " + hallId + ": " + e.getMessage());
        }
    }
    private List<Integer> getValidShowtimeIdsForHall(String hallId) {
        List<Integer> showtimeIds = new ArrayList<>();
        String sql = "SELECT showtime_id FROM Showtimes WHERE hall_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hallId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int showtimeId = rs.getInt("showtime_id");
                showtimeIds.add(showtimeId);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching showtime IDs: " + e.getMessage());
        }
        return showtimeIds;
    }

    public void insertSampleComplaints() {
        String insertSql = "INSERT INTO Complaints (customer_id, description, severity_level, status, assigned_employee_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            // Sample employee IDs for each role
            String salerId = "EMP1000";   // Assume this ID is for a Saler
            String staffId = "EMP1001";   // Assume this ID is for Staff
            String managerId = "EMP1002"; // Assume this ID is for a Manager

            // Prepare data for inserting multiple complaints
            Object[][] sampleComplaints = new Object[][] {
                    {"CST1001", "Audio was not clear in the movie hall.", 1, "open", salerId},
                    {"CST1001", "The screen was blurry.", 1, "open", salerId},
                    {"CST1002", "The seats were uncomfortable.", 2, "open", staffId},
                    {"CST1002", "The air conditioning was too cold.", 2, "open", staffId},
                    {"CST1003", "The movie started late.", 3, "open", managerId}
            };

            // Insert each sample complaint into the database
            for (Object[] complaint : sampleComplaints) {
                pstmt.setString(1, (String) complaint[0]); // Customer ID
                pstmt.setString(2, (String) complaint[1]); // Description
                pstmt.setInt(3, (Integer) complaint[2]);   // Severity level
                pstmt.setString(4, (String) complaint[3]); // Status
                pstmt.setString(5, (String) complaint[4]); // Assigned Employee ID
                pstmt.addBatch();
            }

            // Execute batch insert
            pstmt.executeBatch();
            System.out.println("Sample complaints inserted successfully.");

        } catch (SQLException e) {
            System.out.println("An error occurred while inserting complaints: " + e.getMessage());
        }
    }

    public void displayAllMovies() {
        String query = "SELECT * FROM Movies ORDER BY title ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No movies found.");
                return;
            }

            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                String title = rs.getString("title");
                int duration = rs.getInt("duration");
                String genre = rs.getString("genre");
                int ageRestriction = rs.getInt("age_restriction");
                double price = rs.getDouble("price");
                String subtitles = rs.getString("subtitles");

                // Adjust the printing as per your requirement
                System.out.println("Movie ID: " + movieId + ", Title: " + title + ", Duration: " + duration +
                        " minutes, Genre: " + genre + ", Age Restriction: " + ageRestriction +
                        ", Price: $" + price + ", Subtitles: " + subtitles);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while displaying all movies: " + e.getMessage());
        }
    }
    public void displayAllEmployees() {
        // SQL query to select all columns from the Employees table
        String query = "SELECT * FROM Employees ORDER BY name ASC;";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No employees found in the database.");
                return;
            }

            System.out.println("List of All Employees:");
            System.out.println(String.format("%-10s %-20s %-15s %-10s %-5s", "ID", "Name", "Date of Birth", "Role", "Sales"));

            while (rs.next()) {
                String employeeId = rs.getString("employee_id");
                String name = rs.getString("name");
                String dateOfBirth = rs.getString("date_of_birth");
                String role = rs.getString("role");
                int sales = rs.getInt("sales");

                System.out.println(String.format("%-10s %-20s %-15s %-10s %-5d", employeeId, name, dateOfBirth, role, sales));
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while fetching employees: " + e.getMessage());
        }
    }

    public void searchForMovie() {
        System.out.println("Enter keyword to search for movies (title or genre):");
        String keyword = scanner.nextLine().trim();

        // SQL LIKE operator is used for pattern matching
        String query = "SELECT * FROM Movies WHERE title LIKE ? OR genre LIKE ? ORDER BY title ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Prepare the keyword for search
            String searchKeyword = "%" + keyword + "%";
            pstmt.setString(1, searchKeyword);
            pstmt.setString(2, searchKeyword);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No movies found matching the keyword: " + keyword);
                    return;
                }

                while (rs.next()) {
                    String movieId = rs.getString("movie_id");
                    String title = rs.getString("title");
                    int duration = rs.getInt("duration");
                    String genre = rs.getString("genre");
                    int ageRestriction = rs.getInt("age_restriction");
                    double price = rs.getDouble("price");
                    String subtitles = rs.getString("subtitles");

                    // Adjust the printing as per your requirement
                    System.out.println("Movie ID: " + movieId + ", Title: " + title + ", Duration: " + duration +
                            " minutes, Genre: " + genre + ", Age Restriction: " + ageRestriction +
                            ", Price: $" + price + ", Subtitles: " + subtitles);
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while searching for movies: " + e.getMessage());
        }
    }

    public void displayAllAdvertisements() {
        String query = "SELECT * FROM Advertisements ORDER BY title ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No advertisements found.");
                return;
            }

            System.out.println("List of Advertisements:");
            while (rs.next()) {
                String adId = rs.getString("ad_id");
                String title = rs.getString("title");
                int duration = rs.getInt("duration");
                int ageRestriction = rs.getInt("age_restriction");
                String subtitles = rs.getString("subtitles");

                System.out.println("Ad ID: " + adId + ", Title: " + title + ", Duration: " + duration +
                        " seconds, Age Restriction: " + ageRestriction + ", Subtitles: " + subtitles);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while displaying advertisements: " + e.getMessage());
        }
    }

    public void searchForAdvertisement() {
        System.out.println("Enter the advertisement title to search for:");
        String searchTerm = scanner.nextLine().trim();

        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty.");
            return;
        }

        String query = "SELECT * FROM Advertisements WHERE title LIKE ? ORDER BY title ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No advertisements found matching: " + searchTerm);
                    return;
                }

                System.out.println("Search Results:");
                while (rs.next()) {
                    String adId = rs.getString("ad_id");
                    String title = rs.getString("title");
                    int duration = rs.getInt("duration");
                    int ageRestriction = rs.getInt("age_restriction");
                    String subtitles = rs.getString("subtitles");

                    System.out.println("Ad ID: " + adId + ", Title: " + title + ", Duration: " + duration +
                            " seconds, Age Restriction: " + ageRestriction + ", Subtitles: " + subtitles);
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred during the advertisement search: " + e.getMessage());
        }
    }

    public void displayAllCustomers() {
        String query = "SELECT * FROM Customers ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No customers found in the database.");
                return;
            }

            System.out.println("List of All Customers:");
            while (rs.next()) {
                String customerId = rs.getString("customer_id");
                String name = rs.getString("name");
                String dateOfBirth = rs.getString("date_of_birth");
                int points = rs.getInt("points");
                String paymentMethod = rs.getString("payment_method");

                System.out.println("Customer ID: " + customerId + ", Name: " + name + ", Date of Birth: " + dateOfBirth +
                        ", Points: " + points + ", Payment Method: " + paymentMethod);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving all customers: " + e.getMessage());
        }
    }

    public void addNewCustomer() {
        // Assuming scanner initialization and exception handling are already done elsewhere

        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();

        System.out.print("Enter date of birth (YYYY-MM-DD): ");
        String dateOfBirth = scanner.nextLine();

        System.out.print("Enter points: ");
        int points = Integer.parseInt(scanner.nextLine()); // Handle NumberFormatException as needed

        System.out.println("Payment Method Options: PAYPAL, DEBIT, CREDIT");
        System.out.print("Enter payment method: ");
        String paymentMethod = scanner.nextLine().toUpperCase();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Directly use Customer class to get the next customer ID
        String customerId = Customer.getNextCustomerId();

        String insertSql = "INSERT INTO Customers (customer_id, name, date_of_birth, points, payment_method, password) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            pstmt.setString(1, customerId);
            pstmt.setString(2, name);
            pstmt.setString(3, dateOfBirth);
            pstmt.setInt(4, points);
            pstmt.setString(5, paymentMethod);
            pstmt.setString(6, password);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("New customer added successfully with ID: " + customerId);
            } else {
                System.out.println("Failed to add the new customer.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while adding a new customer: " + e.getMessage());
        }
    }

    public void displayAllReservations() {
        String query = "SELECT Reservations.reservation_id, Reservations.customer_id, " +
                "Reservations.employee_id, Reservations.showtime_id, Reservations.num_seats, " +
                "Reservations.status, Movies.title AS movie_title, CinemaHalls.name AS hall_name, " +
                "Showtimes.start_time " +
                "FROM Reservations " +
                "LEFT JOIN Showtimes ON Reservations.showtime_id = Showtimes.showtime_id " +
                "LEFT JOIN Movies ON Showtimes.movie_id = Movies.movie_id " +
                "LEFT JOIN CinemaHalls ON Showtimes.hall_id = CinemaHalls.hall_id " +
                "ORDER BY Reservations.reservation_id ASC";


        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("No reservations found in the database.");
                return;
            }

            System.out.println("List of All Reservations:");
            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                String customerId = rs.getString("customer_id");
                String employeeId = rs.getString("employee_id");
                int showtimeId = rs.getInt("showtime_id");
                int numSeats = rs.getInt("num_seats");
                String status = rs.getString("status");
                String movieTitle = rs.getString("movie_title");
                String hallName = rs.getString("hall_name");
                String startTime = rs.getString("start_time");

                System.out.println("Reservation ID: " + reservationId + ", Customer ID: " + customerId + ", Employee ID: " + employeeId +
                        ", Showtime ID: " + showtimeId + ", Number of Seats: " + numSeats + ", Status: " + status +
                        ", Movie Title: " + movieTitle + ", Hall Name: " + hallName + ", Start Time: " + startTime);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving all reservations: " + e.getMessage());
        }
    }

    public void cancelReservation() {
        // Display only the reservations for the logged-in customer
        String query = "SELECT * FROM Reservations WHERE customer_id = ? AND status = 'confirmed' ORDER BY reservation_id ASC";
        List<Integer> reservationIds = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, currentUserID);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No reservations to cancel.");
                return;
            }

            System.out.println("Your Reservations:");
            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                int showtimeId = rs.getInt("showtime_id");
                int numSeats = rs.getInt("num_seats");
                String status = rs.getString("status");
                System.out.printf("Reservation ID: %d, Showtime ID: %d, Number of Seats: %d, Status: %s%n",
                        reservationId, showtimeId, numSeats, status);
                reservationIds.add(showtimeId); // Store Showtime IDs, not Reservation IDs
            }

            int showtimeIdToCancel = -1;
            boolean validInput = false;
            while (!validInput) {
                System.out.print("Please enter the Showtime ID of the reservation you wish to cancel (or type 'exit' to cancel): ");
                String input = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(input)) {
                    return; // User chose to exit
                }
                try {
                    showtimeIdToCancel = Integer.parseInt(input);
                    validInput = true; // Input was a valid integer, exit loop
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number for the Showtime ID or type 'exit'.");
                }
            }

            if (!reservationIds.contains(showtimeIdToCancel)) {
                System.out.println("Invalid Showtime ID or no reservations with such Showtime ID.");
                return;
            }

            String updateSql = "UPDATE Reservations SET status = 'cancelled' WHERE showtime_id = ? AND customer_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, showtimeIdToCancel);
                updateStmt.setString(2, currentUserID);

                int affectedRows = updateStmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Your reservation for Showtime ID " + showtimeIdToCancel + " has been successfully cancelled.");
                } else {
                    System.out.println("Failed to cancel the reservation. Please try again.");
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while cancelling the reservation: " + e.getMessage());
        }
    }

    public void cancelReservationByEmployee() {
        // Display all reservations to the employee
        displayAllReservations();

        int reservationId = -1;
        boolean validInput = false;
        while (!validInput) {
            System.out.print("Please enter the reservation ID to cancel (or type 'exit' to cancel): ");
            String input = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(input)) {
                return; // User chose to exit
            }
            try {
                reservationId = Integer.parseInt(input);
                validInput = true; // Input was a valid integer, exit loop
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number for the reservation ID or type 'exit'.");
            }
        }

        // Proceed with the cancellation if the input was valid
        String findReservationSql = "SELECT * FROM Reservations WHERE reservation_id = ? AND status <> 'cancelled'";
        String updateSql = "UPDATE Reservations SET status = 'cancelled', employee_id = ? WHERE reservation_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findReservationSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            findStmt.setInt(1, reservationId);
            ResultSet rs = findStmt.executeQuery();

            if (rs.next()) {
                updateStmt.setString(1, currentUserID); // current logged-in employee's ID
                updateStmt.setInt(2, reservationId);
                int affectedRows = updateStmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Reservation ID " + reservationId + " has been successfully cancelled by employee " + currentUserID);
                } else {
                    System.out.println("Failed to cancel the reservation. Please try again.");
                }
            } else {
                System.out.println("No reservation found with ID " + reservationId + " or it's already cancelled.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while cancelling the reservation: " + e.getMessage());
        }
    }

    public void searchForCustomer() {
        System.out.println("Enter keyword to search for customers (name, customer ID, or date of birth):");
        String keyword = scanner.nextLine().trim();

        // SQL LIKE operator is used for pattern matching
        String query = "SELECT * FROM Customers WHERE customer_id LIKE ? OR name LIKE ? OR date_of_birth LIKE ? ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Prepare the keyword for search
            String searchKeyword = "%" + keyword + "%";
            pstmt.setString(1, searchKeyword);
            pstmt.setString(2, searchKeyword);
            pstmt.setString(3, searchKeyword);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No customers found matching the keyword: " + keyword);
                    return;
                }

                System.out.println("Search Results:");
                while (rs.next()) {
                    // Extract customer data
                    String customerId = rs.getString("customer_id");
                    String name = rs.getString("name");
                    String dateOfBirth = rs.getString("date_of_birth");
                    int points = rs.getInt("points");
                    String paymentMethod = rs.getString("payment_method");
                    // Assuming password is sensitive and should not be displayed

                    // Display each customer matching the search
                    System.out.println("Customer ID: " + customerId + ", Name: " + name +
                            ", Date of Birth: " + dateOfBirth + ", Points: " + points +
                            ", Payment Method: " + paymentMethod);
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while searching for customers: " + e.getMessage());
        }
    }

    public void addNewMovie() {
        System.out.println("\n********** Adding New Movie **********\n");

        System.out.println("Enter title:");
        String title = scanner.nextLine();

        int duration = promptForInt("Enter duration (in minutes):");

        System.out.println("Enter genre:");
        String genre = scanner.nextLine();

        int ageRestriction = promptForInt("Enter age restriction:");

        double price = promptForDouble("Enter price:");

        Movie movie = null;
        boolean isValid = false;

        while (!isValid) {
            System.out.println("--------- Choose Subtitle Language --------\n");
            System.out.println("\t1. English (EN)");
            System.out.println("\t2. French (FR)");
            System.out.println("\nEnter an option: ");
            String subtitleChoice = scanner.nextLine();

            switch (subtitleChoice) {
                case "1":
                    movie = AbstractFactory.factory(Language.EN).createMovie(title, duration, genre, ageRestriction, price);
                    isValid = true;
                    break;
                case "2":
                    movie = AbstractFactory.factory(Language.FR).createMovie(title, duration, genre, ageRestriction, price);
                    isValid = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose again.");
                    break;
            }
        }

        if (movie != null) {
            insertMovie(movie); // Assuming insertMovie handles the insertion logic
        } else {
            System.out.println("Failed to create a new movie. Please try again.");
        }
    }

    public void addNewAdvertisement() {
        System.out.println("\n********** Adding New Advertisement **********\n");

        System.out.println("Enter advertisement title:");
        String title = scanner.nextLine();

        int duration = promptForInt("Enter duration (in seconds):");

        int ageRestriction = promptForInt("Enter age restriction:");

        Advertisement advertisement = null;
        boolean isValid = false;

        while (!isValid) {
            System.out.println("--------- Choose Subtitle Language --------\n");
            System.out.println("\t1. English (EN)");
            System.out.println("\t2. French (FR)");
            System.out.println("\nEnter an option: ");
            String subtitleChoice = scanner.nextLine();

            switch (subtitleChoice) {
                case "1":
                    advertisement = AbstractFactory.factory(Language.EN).createAdvertisement(title, duration, ageRestriction);
                    isValid = true;
                    break;
                case "2":
                    advertisement = AbstractFactory.factory(Language.FR).createAdvertisement(title, duration, ageRestriction);
                    isValid = true;
                    break;
                default:
                    System.out.println("Invalid option. Please choose again.");
                    break;
            }
        }

        if (advertisement != null) {
            insertAdvertisement(advertisement); // Assuming insertAdvertisement handles the insertion logic
        } else {
            System.out.println("Failed to create a new advertisement. Please try again.");
        }
    }

    public void addNewEmployee() {
        System.out.println("\n********** Adding New Employee **********\n");

        System.out.println("Enter name:");
        String name = scanner.nextLine();

        System.out.println("Enter date of birth (YYYY-MM-DD):");
        String dateOfBirth = scanner.nextLine();

        System.out.println("Enter password:");
        String password = scanner.nextLine();

        Employee employee = null;
        boolean isValid = false;

        while (!isValid) {
            System.out.println("--------- Choose Role --------\n");
            System.out.println("\t1. Saler");
            System.out.println("\t2. Staff");
            System.out.println("\t3. Manager");
            System.out.println("\nEnter an option: ");
            String roleChoice = scanner.nextLine();
            EmployeeMaker employeeMaker = null;

            switch (roleChoice) {
                case "1":
                    employeeMaker = new EmployeeMaker(new SalerEmpBuilder());
                    break;
                case "2":
                    employeeMaker = new EmployeeMaker(new StaffEmpBuilder());
                    break;
                case "3":
                    employeeMaker = new EmployeeMaker(new ManagerEmpBuilder());
                    break;
                default:
                    System.out.println("Invalid option. Please choose again.");
                    continue;
            }

            employeeMaker.makeEmployee(name, dateOfBirth, password);
            employee = employeeMaker.getEmployee();
            isValid = true;
        }

        if (employee != null) {
            insertEmployee(employee); // Assuming insertEmployee handles the insertion logic
        } else {
            System.out.println("Failed to create a new employee. Please try again.");
        }
    }

    public void removeMovie() {
        System.out.println("\n********** Remove a Movie **********\n");

        // Display all movies to help user decide which one to remove
        displayAllMovies();

        System.out.println("Enter the Movie ID of the movie you wish to remove:");
        String movieId = scanner.nextLine();

        // SQL statement for deleting a movie
        String sql = "DELETE FROM Movies WHERE movie_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, movieId);
            int affectedRows = pstmt.executeUpdate();

            // Check if a movie was deleted
            if (affectedRows > 0) {
                System.out.println("Movie with ID " + movieId + " was successfully removed.");
            } else {
                System.out.println("No movie with ID " + movieId + " was found.");
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while trying to remove a movie: " + e.getMessage());
        }
    }

    public void removeAdvertisement() {
        System.out.println("\n********** Remove an Advertisement **********\n");

        // Display all advertisements to help the user decide which one to remove
        displayAllAdvertisements();

        System.out.println("Enter the Advertisement ID of the advertisement you wish to remove:");
        String adId = scanner.nextLine();

        // SQL statement for deleting an advertisement
        String sql = "DELETE FROM Advertisements WHERE ad_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adId);
            int affectedRows = pstmt.executeUpdate();

            // Check if an advertisement was deleted
            if (affectedRows > 0) {
                System.out.println("Advertisement with ID " + adId + " was successfully removed.");
            } else {
                System.out.println("No advertisement with ID " + adId + " was found.");
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while trying to remove an advertisement: " + e.getMessage());
        }
    }

    public void displayMoviesBasedOnAge() {
        if (this.currentUserID == null) {
            System.out.println("No user is currently logged in.");
            return;
        }

        LocalDate dob = getCustomerDOB(this.currentUserID);
        if (dob == null) {
            System.out.println("Customer not found or DOB not available.");
            return;
        }
        LocalDate currentDate = LocalDate.now();
        int age = Period.between(dob, currentDate).getYears();

        String sql = "SELECT * FROM Movies WHERE age_restriction <= ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, age);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("No suitable movies found for your age.");
                return;
            }
            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                String title = rs.getString("title");
                int duration = rs.getInt("duration");
                String genre = rs.getString("genre");
                int ageRestriction = rs.getInt("age_restriction");
                double price = rs.getDouble("price");
                String subtitles = rs.getString("subtitles");
                System.out.printf("Movie ID: %s, Title: %s, Duration: %d minutes, Genre: %s, Age Restriction: %d, Price: $%.2f, Subtitles: %s%n",
                        movieId, title, duration, genre, ageRestriction, price, subtitles);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching movies: " + e.getMessage());
        }
    }

    // Helper method to fetch customer's date of birth
    private LocalDate getCustomerDOB(String customerId) {
        String sql = "SELECT date_of_birth FROM Customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dobStr = rs.getString("date_of_birth");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                    return LocalDate.parse(dobStr, formatter);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching customer DOB: " + e.getMessage());
        }
        return null;
    }

    private int[] getHallLayout(String hallId) throws SQLException {
        String sql = "SELECT rows, seats_per_row FROM CinemaHalls WHERE hall_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hallId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("rows"), rs.getInt("seats_per_row")};
                }
            }
        }
        return new int[]{0, 0}; // In case the hall is not found
    }

    public void displaySeatMatrixWithNumbers(String hallId, int showtimeId) {
        try {
            int[] layout = getHallLayout(hallId);
            int rows = layout[0];
            int seatsPerRow = layout[1];

            System.out.println();
            System.out.print("//////////////////////////\n");
            System.out.print("//                      //\n");
            System.out.print("//        SCREEN        //\n");
            System.out.print("//                      //\n");
            System.out.print("//////////////////////////\n");
            System.out.println();


            for (char row = 'A'; row < 'A' + rows; row++) {
                System.out.print(row + "  "); // Row letter at the start of the row
                for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                    String seatStatus = getSeatStatus(hallId, row, seatNum, showtimeId);
                    System.out.print(seatStatus + " ");
                }
                System.out.println(" " + row); // Row letter at the end of the row
            }

            // Display column footers (seat numbers)
            System.out.print("   "); // Align the footer
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                System.out.print(seatNum + " ");
            }
            System.out.println(); // New line at the end
        } catch (SQLException e) {
            System.out.println("An error occurred while displaying seat matrix: " + e.getMessage());
        }
    }


    private String getSeatStatus(String hallId, char row, int seatNum, int showtimeId) throws SQLException {
        String sql = "SELECT status FROM Seats WHERE hall_id = ? AND row = ? AND seat_number = ? AND showtime_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hallId);
            pstmt.setString(2, String.valueOf(row));
            pstmt.setInt(3, seatNum);
            pstmt.setInt(4, showtimeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status").equals("available") ? "O" : "X"; // Use 'O' for available seats
                }
            }
        }
        return "O"; // Default to available if not found
    }
    public void reserveMovie() {
        displayMoviesBasedOnAge();
        System.out.println("Enter the Movie ID to reserve:");
        String movieId = scanner.nextLine().trim();
        boolean showtimesAvailable = displayShowtimesForMovie(movieId);
        if (!showtimesAvailable) {
            return;
        }

        System.out.println("Enter the Showtime ID for the movie you wish to reserve:");
        int showtimeId;
        try {
            showtimeId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for Showtime ID.");
            return;
        }

        String hallId = getHallIdForShowtime(showtimeId);
        if (hallId == null) {
            System.out.println("No hall found for the selected showtime.");
            return;
        }

        int availableSeats = getAvailableSeats(hallId, showtimeId);
        if (availableSeats <= 0) {
            System.out.println("Sorry, this showtime is fully booked.");
            return;
        }

        System.out.println("How many seats would you like to reserve?");
        int seatsToReserve = Integer.parseInt(scanner.nextLine().trim());
        if (seatsToReserve > availableSeats) {
            System.out.println("There are not enough seats available. Only " + availableSeats + " seats are available.");
            return;
        }

        List<String> selectedSeats = new ArrayList<>();
        for (int i = 0; i < seatsToReserve; i++) {
            displaySeatMatrixWithNumbers(hallId, showtimeId);
            System.out.println("Enter the seat number (e.g., A1) for seat " + (i + 1) + ":");
            String seat = scanner.nextLine().trim().toUpperCase();
            selectedSeats.add(seat);
        }

        boolean reservationSuccess = reserveSeatsForShowtime_CustomerUsing(currentUserID, showtimeId, hallId, selectedSeats.toArray(new String[0]));
        if (reservationSuccess) {
            System.out.println("Your seats have been successfully reserved.");

            // Increase points by 10 for each reserved seat
            Customer customer = new Customer(); // Customer class has a method to set ID without incrementing the counter
            customer.setId(currentUserID);
            customer.increasePoints(seatsToReserve * 10);

        } else {
            System.out.println("Failed to reserve the selected seats. They may be taken or your input was invalid.");
        }
    }
    public boolean reserveSeatsForShowtime_CustomerUsing(String customerId, int showtimeId, String hallId, String[] selectedSeats) {
        Connection conn = null;
        PreparedStatement selectSalerStmt = null;
        PreparedStatement reservationStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        boolean reservationSuccess = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction block

            // Select a random Saler employee ID
            String selectSalerSql = "SELECT employee_id FROM Employees WHERE role = 'SALER' ORDER BY RANDOM() LIMIT 1";
            selectSalerStmt = conn.prepareStatement(selectSalerSql);
            rs = selectSalerStmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("No Saler available for reservation.");
            }
            String employeeId = rs.getString("employee_id");

            // Insert a new reservation
            String reservationSql = "INSERT INTO Reservations (customer_id, showtime_id, employee_id, num_seats, status) VALUES (?, ?, ?, ?, 'confirmed')";
            reservationStmt = conn.prepareStatement(reservationSql, Statement.RETURN_GENERATED_KEYS);
            reservationStmt.setString(1, customerId);
            reservationStmt.setInt(2, showtimeId);
            reservationStmt.setString(3, employeeId);
            reservationStmt.setInt(4, selectedSeats.length);
            int affectedRows = reservationStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating reservation failed, no rows affected.");
            }

            // Retrieve the reservation ID from the generated keys
            rs = reservationStmt.getGeneratedKeys();
            if (!rs.next()) {
                throw new SQLException("Creating reservation failed, no ID obtained.");
            }
            int reservationId = rs.getInt(1);

            // Reserve the seats
            String placeholders = String.join(",", Collections.nCopies(selectedSeats.length, "?"));
            String updateSql = "UPDATE Seats SET status = 'reserved', reservation_id = ? WHERE hall_id = ? AND row || seat_number IN (" + placeholders + ") AND status = 'available' AND showtime_id = ?";
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, reservationId);
            updateStmt.setString(2, hallId);
            for (int i = 0; i < selectedSeats.length; i++) {
                updateStmt.setString(i + 3, selectedSeats[i]);
            }
            updateStmt.setInt(selectedSeats.length + 3, showtimeId);
            affectedRows = updateStmt.executeUpdate();
            if (affectedRows != selectedSeats.length) {
                throw new SQLException("Not all seats could be reserved.");
            }

            conn.commit();  // Everything was successful, commit the transaction
            reservationSuccess = true;

            // Update the customer's points
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.increasePoints(selectedSeats.length * 10);
        } catch (SQLException ex) {
            System.out.println("SQL Exception: " + ex.getMessage());
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on error
                }
            } catch (SQLException rollbackEx) {
                System.out.println("SQL Exception on rollback: " + rollbackEx.getMessage());
            }
        } finally {
            // Close all resources
            try {
                if (rs != null) rs.close();
                if (selectSalerStmt != null) selectSalerStmt.close();
                if (reservationStmt != null) reservationStmt.close();
                if (updateStmt != null) updateStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close(); // Close connection
                }
            } catch (SQLException ex) {
                System.out.println("SQL Exception on closing resources: " + ex.getMessage());
            }
        }
        return reservationSuccess;
    }
    public void reserveMovieForCustomers() {
        // Display all customers for the employee to choose from
        displayAllCustomers();
        System.out.println("Enter the Customer ID you are helping to reserve a ticket for:");
        String customerId = scanner.nextLine().trim();

        // Validate the customer ID
        if (!validateCustomerId(customerId)) {
            System.out.println("Invalid Customer ID. Please try again.");
            return;
        }

        // Select a movie
        displayAllMovies();
        System.out.println("Enter the Movie ID to reserve:");
        String movieId = scanner.nextLine().trim();

        // Display showtimes for the selected movie
        if (!displayShowtimesForMovie(movieId)) {
            return; // Exit if no showtimes are available
        }

        System.out.println("Enter the Showtime ID for the movie you wish to reserve:");
        int showtimeId = Integer.parseInt(scanner.nextLine().trim());

        // Fetch the hall ID for the selected showtime
        String hallId = getHallIdForShowtime(showtimeId);
        if (hallId == null) {
            System.out.println("No hall found for the selected showtime.");
            return;
        }

        // Check available seats before displaying the seat matrix
        int availableSeats = getAvailableSeats(hallId, showtimeId);
        if (availableSeats <= 0) {
            System.out.println("Sorry, this showtime is fully booked.");
            return;
        }

        System.out.println("There are " + availableSeats + " seats available.");
        System.out.println("Enter the number of seats you wish to reserve:");
        int seatsToReserve = Integer.parseInt(scanner.nextLine().trim());

        if (seatsToReserve > availableSeats) {
            System.out.println("Cannot reserve more seats than available.");
            return;
        }

        List<String> reservedSeats = new ArrayList<>();
        for (int i = 0; i < seatsToReserve; i++) {
            displaySeatMatrixWithNumbers(hallId, showtimeId); // Method that shows matrix with row letters and column numbers
            System.out.println("Select seat " + (i + 1) + " (e.g., A1):");
            String seatSelection = scanner.nextLine().trim().toUpperCase();

            reservedSeats.add(seatSelection);
        }

        if (reserveSeatsForShowtime(customerId, showtimeId, hallId, reservedSeats.toArray(new String[0]), this.currentUserID)) {
            System.out.println("Seats have been successfully reserved and sales increased for the assisting employee.");
        } else {
            System.out.println("Failed to reserve seats.");
        }
    }

    private boolean validateCustomerId(String customerId) {
        // Check if the customer ID exists in the Customers table
        String query = "SELECT COUNT(*) FROM Customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while validating Customer ID: " + e.getMessage());
        }
        return false;
    }

    private int getAvailableSeats(String hallId, int showtimeId) {
        // Query to count the number of reserved seats for a showtime
        String sql = "SELECT COUNT(*) FROM Seats WHERE hall_id = ? AND showtime_id = ? AND status = 'reserved'";
        int totalSeats = getHallCapacity(hallId); // You need to implement this method to get total seats for a hall
        int reservedSeats = 0;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hallId);
            pstmt.setInt(2, showtimeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                reservedSeats = rs.getInt(1);
            }
            return totalSeats - reservedSeats;
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching available seats: " + e.getMessage());
            return 0;
        }
    }
    private int getHallCapacity(String hallId) {
        String sql = "SELECT capacity FROM CinemaHalls WHERE hall_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hallId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("capacity");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching hall capacity: " + e.getMessage());
        }
        return 0; // Default to 0 if not found or error
    }
    public boolean displayShowtimesForMovie(String movieId) {
        boolean showtimesAvailable = false;
        String query = "SELECT * FROM Showtimes WHERE movie_id = ? ORDER BY showtime_id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, movieId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No showtimes found for Movie ID " + movieId);
            } else {
                showtimesAvailable = true;
                System.out.println("Showtimes for Movie ID " + movieId + ":");
                while (rs.next()) {
                    int showtimeId = rs.getInt("showtime_id");
                    String hallId = rs.getString("hall_id");
                    String startTime = rs.getString("start_time");
                    String endTime = rs.getString("end_time");
                    System.out.printf("Showtime ID: %d, Hall ID: %s, Start Time: %s, End Time: %s%n",
                            showtimeId, hallId, startTime, endTime);
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching showtimes for movie: " + e.getMessage());
        }
        return showtimesAvailable;
    }

    public String getHallIdForShowtime(int showtimeId) {
        String query = "SELECT hall_id FROM Showtimes WHERE showtime_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, showtimeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("hall_id");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching hall ID: " + e.getMessage());
        }

        return null; // Return null if the hall ID was not found or if there was an error
    }

    public boolean reserveSeatsForShowtime(String customerId, int showtimeId, String hallId, String[] selectedSeats, String employeeId) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            // First, insert a new reservation
            String reservationSql = "INSERT INTO Reservations (customer_id, showtime_id, employee_id, num_seats, status) VALUES (?, ?, ?, ?, 'confirmed')";
            int reservationId;
            try (PreparedStatement reservationStmt = conn.prepareStatement(reservationSql, Statement.RETURN_GENERATED_KEYS)) {
                reservationStmt.setString(1, customerId);
                reservationStmt.setInt(2, showtimeId);
                reservationStmt.setString(3, employeeId);
                reservationStmt.setInt(4, selectedSeats.length);
                int affectedRows = reservationStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating reservation failed, no rows affected.");
                }
                try (ResultSet generatedKeys = reservationStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservationId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating reservation failed, no ID obtained.");
                    }
                }
            }

            // Next, update seat status to 'reserved' and link them to the reservation
            String updateSql = String.format("UPDATE Seats SET status = 'reserved', reservation_id = ? " +
                            "WHERE hall_id = ? AND row || seat_number IN (%s) " +
                            "AND status = 'available' AND showtime_id = ?",
                    String.join(",", Collections.nCopies(selectedSeats.length, "?")));
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, reservationId);
                updateStmt.setString(2, hallId);
                for (int i = 0; i < selectedSeats.length; i++) {
                    updateStmt.setString(3 + i, selectedSeats[i]);
                }
                updateStmt.setInt(3 + selectedSeats.length, showtimeId);

                int seatsUpdated = updateStmt.executeUpdate();
                if (seatsUpdated != selectedSeats.length) {
                    throw new SQLException("Not all seats could be reserved. Only " + seatsUpdated + " were updated.");
                }
            }
            // New Employee instance to update sales
            Employee assistingEmployee = new Employee(employeeId);
            assistingEmployee.increaseSales(selectedSeats.length * 10); // Update sales here, adjust the sales value accordingly

            // If everything was successful, commit the transaction
            conn.commit();
            return true;
        } catch (SQLException ex) {
            System.out.println("Reservation failed: " + ex.getMessage());
            ex.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back.");
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Rollback failed: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println("Could not reset auto-commit or close connection: " + ex.getMessage());
            }
        }
    }



    private int promptForInt(String message) {
        while (true) {
            System.out.println(message);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private double promptForDouble(String message) {
        while (true) {
            System.out.println(message);
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    public String getEmployeeIdByRole(String role) {
        String sql = "SELECT employee_id FROM Employees WHERE role = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("employee_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching employee ID by role: " + e.getMessage());
        }
        return null;
    }

    public void setupComplaintHandlingChain() {
        // Fetch employee IDs based on role
        String salerId = getEmployeeIdByRole("SALER");
        String staffId = getEmployeeIdByRole("STAFF");
        String managerId = getEmployeeIdByRole("MANAGER");

        // Check if any ID is null and handle accordingly (e.g., log an error)
        if (salerId == null || staffId == null || managerId == null) {
            System.out.println("Error: Could not fetch all necessary employee IDs for the complaint handling chain.");
            return;
        }

        // Create handler instances
        SalerHandler salerHandler = new SalerHandler(salerId);
        StaffHandler staffHandler = new StaffHandler(staffId);
        ManagerHandler managerHandler = new ManagerHandler(managerId);

        // Set up the chain
        salerHandler.setSuccessor(staffHandler);
        staffHandler.setSuccessor(managerHandler);

        // Store the head of the chain for later use
        this.complaintChainHead = salerHandler;
    }

    public void sendComplaint() {
        // Assume customer is logged in and `currentUserID` holds the customer's ID
        System.out.println("Enter your complaint description:");
        String description = scanner.nextLine().trim();

        System.out.println("Enter the severity level (1 for Saler, 2 for Staff, 3 for Manager):");
        int severityLevel = Integer.parseInt(scanner.nextLine().trim());

        // Create a complaint instance (initially with no assigned employee or resolution)
        Complaint complaint = new Complaint(currentUserID, description, severityLevel);

        // Insert complaint into the database and retrieve generated complaint ID
        int complaintId = insertComplaintIntoDatabase(complaint);
        if (complaintId == -1) {
            System.out.println("Failed to send complaint.");
            return;
        }

        // Set the complaint ID
        complaint.setId(complaintId);

        // Send the complaint through the chain
        complaintChainHead.handleComplaint(complaint);
    }
    private int insertComplaintIntoDatabase(Complaint complaint) {
        String insertSql = "INSERT INTO Complaints (customer_id, description, severity_level, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, complaint.getCustomerId());
            pstmt.setString(2, complaint.getDescription());
            pstmt.setInt(3, complaint.getSeverityLevel());
            pstmt.setString(4, complaint.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting complaint into database: " + e.getMessage());
        }
        return -1;
    }
    public boolean viewAssignedComplaints() {
        if (this.currentUserID == null) {
            System.out.println("No employee is currently logged in.");
            return false;
        }

        String query = "SELECT * FROM Complaints WHERE assigned_employee_id = ? AND status = 'open'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, this.currentUserID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No complaints assigned to you.");
                    return false;
                }

                while (rs.next()) {
                    int complaintId = rs.getInt("complaint_id");
                    String customerId = rs.getString("customer_id");
                    String description = rs.getString("description");
                    int severityLevel = rs.getInt("severity_level");
                    System.out.println("Complaint ID: " + complaintId + ", Customer ID: " + customerId +
                            ", Description: " + description + ", Severity Level: " + severityLevel);
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving assigned complaints: " + e.getMessage());
            return false;
        }
    }    public void handleAndResolveComplaint() {
        boolean hasComplaints = viewAssignedComplaints();

        if (!hasComplaints) {
            // No complaints to handle, return to main menu
            return;
        }

        System.out.println("Enter the ID of the complaint you want to handle:");
        try {
            int complaintId = Integer.parseInt(scanner.nextLine().trim());
            // Ask for resolution text
            System.out.println("Enter your resolution for the complaint:");
            String resolution = scanner.nextLine().trim();

            // Update the complaint status and resolution in the database
            updateComplaintResolution(complaintId, resolution);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid complaint ID.");
        }
    }
    private void updateComplaintResolution(int complaintId, String resolution) {
        String updateSql = "UPDATE Complaints SET status = 'resolved', resolution = ?, assigned_employee_id = ? WHERE complaint_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setString(1, resolution);
            pstmt.setString(2, this.currentUserID); //logged-in employee
            pstmt.setInt(3, complaintId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Complaint resolved successfully!");
            } else {
                System.out.println("Failed to resolve complaint.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating complaint resolution: " + e.getMessage());
        }
    }



}