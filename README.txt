README.txt - Cinema Management System
=====================================

Program Description
-------------------
This program is a demonstration of applying key design patterns such as Abstract Factory, Builder, Chain of Responsibility, and Singleton within a cinema management context. The goal is to simulate interactions between customers, employees, and a cinema database system.

Design Patterns Used:
1. Abstract Factory: Used to create movie and advertisement objects in two different languages, French (FR) and English (EN).

2. Builder: Used to construct various User types including Customers and Employees (Saler, Staff, Manager) with varying attributes.

3. Chain of Responsibility: Facilitates the passing of customer complaints with three levels of severity, each assigned to be handled by the appropriate role - Saler for level 1, Staff for level 2, and Manager for level 3 complaints.

4. Singleton: Ensures a single instance of the database connection is created and utilized throughout the program, promoting efficient resource management.

Installation Instructions:
--------------------------

1. Ensure you have Java Runtime Environment (JRE) or Java Development Kit (JDK) installed on your system. The application requires Java version 8 or higher.

2. Download the `sqlite-jdbc-3.30.1.jar` from the SQLite JDBC official website or a trusted source. (this rar has already included sqlite-jdbc-3.30.1.jar)

3. Add the downloaded `sqlite-jdbc-3.30.1.jar` file to your project's library path. If you are using an Integrated Development Environment (IDE) like IntelliJ IDEA or Eclipse, you can add the JAR by accessing the Project Structure or Build Path settings.

4. Navigate to the directory containing the `Main.java` file in your project.

5. Compile the program by running the following command in your terminal or command prompt (skip this step if you're using an IDE with built-in compilation):

6. Execute the compiled program by running Main


7. Upon running the Main program, please wait as the application initializes the database and populates it with sample data. This process may take a few moments.

8. Once the initialization is complete, the application will display a message indicating that the `cinema_db.sqlite` database has been created and populated successfully. You should find this database file in the same directory as your program.

9. You can now proceed to use the application as per the user instructions detailed in the Usage section of this README.


Login Credentials:
--------------------------

- Manager: Employee ID "EMP1002", Password "pwd123"

- Staff: Employee ID "EMP1001", Password "secret123"

- Saler: Employee ID "EMP1000", Password "123"


User Menu Access:
--------------------------

- Salers and Staff have common menu options for operations like displaying movies, searching for movies and advertisements, viewing customer details, and adding new customers.

- Managers have additional privileges such as adding or removing movies and advertisements, managing employee records, etc.

- Customers can view movies based on age restrictions, search for movies, make reservations, cancel reservations (they have reserved before with logged in credential), and send complaints.


Points and Sales System:
--------------------------

- Employee sales increase based on the number of seats they help reserve, calculated using the formula: Sales = 10 x Number of Seats.

- Customer points increase similarly when they reserve seats themselves.

Additional Features:
--------------------------

- Employees can view and manage complaints related to their role and severity level they handle. Saler handles complaint with severity level 1, Staff handles complaint with severity level 2, Manager handlers complaint with severity level 3.

Considerations for Future Development:
--------------------------
1. Integrate a rewards system allowing employees to utilize their earned sales points.

2. Allow customers to redeem their points, which could be linked to the pricing of movies, providing additional value to the program.


Known Exceptions Needed To Be Handled:
--------------------------
1. Duplicate seat reservations may cause an SQL Exception.

