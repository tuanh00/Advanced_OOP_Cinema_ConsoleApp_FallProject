package User.Employee;

public interface IEmployeeBuilder {
    /*      "CREATE TABLE IF NOT EXISTS Employees (" +
                        "employee_id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "date_of_birth TEXT NOT NULL," +
                        "role TEXT NOT NULL," +
                        "sales INTEGER NOT NULL DEFAULT 0," +
                        "password TEXT NOT NULL);",
*/
    void setName(String var1);
    void setBirthdate(String var2);
    void setRole();
    void setPassword(String var3);
    Employee getEmployee();

}
