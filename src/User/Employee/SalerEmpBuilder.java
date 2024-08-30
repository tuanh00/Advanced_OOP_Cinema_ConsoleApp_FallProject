package User.Employee;

import Enums.Roles;

public class SalerEmpBuilder implements IEmployeeBuilder{
    Employee employee = new Employee();

    @Override
    public void setName(String var1) {
        this.employee.setName(var1);
    }

    @Override
    public void setBirthdate(String var2) {
        this.employee.setBirthdate(var2);
    }

    @Override
    public void setRole() {
        this.employee.setRole(Roles.SALER);
    }


    @Override
    public void setPassword(String var3) {
        this.employee.setPassword(var3);
    }

    @Override
    public Employee getEmployee() {
        return this.employee;
    }
}
