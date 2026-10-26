package ru.mirea.movingcompany.gall.services;

import ru.mirea.movingcompany.gall.enums.EmployeeRoles;
import ru.mirea.movingcompany.gall.exceptions.EmplyeeTitleException;
import ru.mirea.movingcompany.gall.models.EmolyeeModel;
import ru.mirea.movingcompany.gall.repositories.EmployeeRepository;

import java.util.List;
import java.util.Optional;

public class EmployeeService {
    private EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmolyeeModel> getAllEmployees() {
        return employeeRepository.getAllEmployees();
    }

    public Optional<EmolyeeModel> getEmployeeById(int id) {
        return employeeRepository.getEmployeeById(id);
    }

    public List<EmolyeeModel> getEmployeesByRole(EmployeeRoles role) {
        return employeeRepository.getEmployeesByRole(role);
    }

    public Optional<EmolyeeModel> getEmployeeByPhone(String phone) {
        return employeeRepository.getEmployeeByPhone(phone);
    }

    public List<EmolyeeModel> getEmployeesByName(String name) {
        return employeeRepository.getEmployeesByName(name);
    }

    public boolean createEmployee(EmolyeeModel employee) {
        if (employee.getFullName() == null || employee.getFullName().isBlank()) {
            throw new EmplyeeTitleException("ФИО сотрудника не может быть пустым!");
        }
        return employeeRepository.createEmployee(employee);
    }

    public EmolyeeModel updateEmployee(EmolyeeModel employee) {
        if (employee.getFullName() == null || employee.getFullName().isBlank()) {
            throw new EmplyeeTitleException("ФИО сотрудника не может быть пустым!");
        }
        return employeeRepository.updateEmployee(employee);
    }

    public EmolyeeModel deleteEmployeeById(int id) {
        return employeeRepository.deleteEmployeeById(id);
    }

    public EmolyeeModel updateRole(int id, EmployeeRoles role) {
        Optional<EmolyeeModel> employeeModelOptional = employeeRepository.getEmployeeById(id);
        if (employeeModelOptional.isEmpty()) {
            return null;
        }
        EmolyeeModel employeeModel = employeeModelOptional.get();
        employeeModel.setRole(role);
        return this.updateEmployee(employeeModel);
    }
}
