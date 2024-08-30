package User.Employee;

public class EmployeeMaker {
    /**
     *   void setName(String var1);
     *     void setBirthdate(String var2);
     *     void setRole();
     *     void setSales();
     *     void setPassword(String var3);
     */
    private IEmployeeBuilder iEmployeeBuilder;
    public EmployeeMaker(IEmployeeBuilder iEmployeeBuilder) {
        this.iEmployeeBuilder = iEmployeeBuilder;
    }
    public void makeEmployee(String name, String birthdate, String password){
        this.iEmployeeBuilder.setName(name);
        this.iEmployeeBuilder.setBirthdate(birthdate);
        this.iEmployeeBuilder.setRole();
        this.iEmployeeBuilder.setPassword(password);
    }
    public Employee getEmployee(){return this.iEmployeeBuilder.getEmployee();}
    public void reset(IEmployeeBuilder iEmployeeBuilder) {
        this.iEmployeeBuilder = iEmployeeBuilder;
    }

}
