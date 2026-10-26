package ru.mirea.movingcompany.gall.repositories;

import ru.mirea.movingcompany.gall.enums.EmployeeRoles;
import ru.mirea.movingcompany.gall.models.EmolyeeModel;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    List<EmolyeeModel> getAllEmployees();
    Optional<EmolyeeModel> getEmployeeById(int id);
    List<EmolyeeModel> getEmployeesByRole(EmployeeRoles role);
    Optional<EmolyeeModel> getEmployeeByPhone(String phone);
    List<EmolyeeModel> getEmployeesByName(String name);

    boolean createEmployee(EmolyeeModel employee);
    EmolyeeModel updateEmployee(EmolyeeModel employee);

    EmolyeeModel deleteEmployeeById(int id);
}
