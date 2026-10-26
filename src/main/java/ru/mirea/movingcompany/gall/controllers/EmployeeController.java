package ru.mirea.movingcompany.gall.controllers;

import ru.mirea.movingcompany.gall.models.EmolyeeModel;
import ru.mirea.movingcompany.gall.enums.EmployeeRoles;
import ru.mirea.movingcompany.gall.services.EmployeeService;
import ru.mirea.movingcompany.gall.repositories.JdbcEmployeeRepository;
import ru.mirea.movingcompany.controllers.ViewSupport;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class EmployeeController {

    private final EmployeeService employeeService = new EmployeeService(new JdbcEmployeeRepository());

    @FXML private TableView<EmolyeeModel> employeeTable;
    @FXML private TableColumn<EmolyeeModel, Number> idEmployee;
    @FXML private TableColumn<EmolyeeModel, String> fullNameEmployee;
    @FXML private TableColumn<EmolyeeModel, String> phoneEmployee;
    @FXML private TableColumn<EmolyeeModel, String> roleEmployee;
    @FXML private TableColumn<EmolyeeModel, Number> salaryEmployee;
    @FXML private Button createEmployeeButton;
    @FXML private Button updateEmployeeButton;
    @FXML private Button deleteEmployeeButton;
    @FXML private Button refreshEmployeeButton;
    @FXML private Label statusLabel;

    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        idEmployee.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getId()));
        fullNameEmployee.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getFullName()));
        phoneEmployee.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPhone()));
        roleEmployee.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ViewSupport.label(cell.getValue().getRole())));
        salaryEmployee.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getSalary()));
        employeeTable.setPlaceholder(new Label("Сотрудников пока нет"));
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        employeeTable.disableProperty().bind(busy);
        createEmployeeButton.disableProperty().bind(busy);
        refreshEmployeeButton.disableProperty().bind(busy);
        updateEmployeeButton.disableProperty().bind(busy.or(employeeTable.getSelectionModel().selectedItemProperty().isNull()));
        deleteEmployeeButton.disableProperty().bind(updateEmployeeButton.disableProperty());
        loadEmployees();
    }

    private void loadEmployees() {
        busy.set(true);
        statusLabel.setText("Загрузка сотрудников…");
        ViewSupport.runTask(employeeService::getAllEmployees, employees -> {
            employeeTable.getItems().setAll(employees);
            statusLabel.setText("Сотрудников: " + employees.size());
            busy.set(false);
        }, this::showError);
    }

    @FXML
    private void onCreateEmployeeButtonClick() {
        showEmployeeDialog(null).ifPresent(employee -> saveEmployee(employee, true));
    }

    @FXML
    private void onUpdateEmployeeButtonClick() {
        EmolyeeModel employee = employeeTable.getSelectionModel().getSelectedItem();
        if (employee != null) {
            showEmployeeDialog(employee).ifPresent(updated -> saveEmployee(updated, false));
        }
    }

    private Optional<EmolyeeModel> showEmployeeDialog(EmolyeeModel employee) {
        boolean create = employee == null;
        Dialog<ButtonType> dialog = new Dialog<>();
        ViewSupport.prepareDialog(dialog, employeeTable);
        dialog.setTitle(create ? "Создание сотрудника" : "Редактирование сотрудника");
        dialog.setHeaderText(create ? "Новый сотрудник" : "Сотрудник №" + employee.getId());

        ButtonType saveButton = new ButtonType(
                "Сохранить", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton, new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE)
        );


        TextField nameField = new TextField(create ? "" : employee.getFullName());
        TextField phoneField = new TextField(create ? "" : employee.getPhone());
        ComboBox<EmployeeRoles> roleField = new ComboBox<>();
        roleField.getItems().addAll(EmployeeRoles.values());
        roleField.setValue(create ? EmployeeRoles.LOADER : employee.getRole());
        ViewSupport.setChoiceLabels(roleField, ViewSupport::label);
        TextField salaryField = new TextField(create ? "" : Integer.toString(employee.getSalary()));
        salaryField.setPromptText("В рублях, целое число");

        GridPane formCreateEmployee = new GridPane();
        formCreateEmployee.setHgap(10);
        formCreateEmployee.setVgap(10);
        formCreateEmployee.setPadding(new Insets(20));

        formCreateEmployee.addRow(0, new Label("ФИО:"), nameField);
        formCreateEmployee.addRow(1, new Label("Телефон:"), phoneField);
        formCreateEmployee.addRow(2, new Label("Должность:"), roleField);
        formCreateEmployee.addRow(3, new Label("Зарплата:"), salaryField);

        dialog.getDialogPane().setContent(formCreateEmployee);

        ViewSupport.validateDialog(dialog, saveButton, () -> {
            ViewSupport.requireText(nameField.getText(), "ФИО");
            if (!roleField.getItems().contains(roleField.getValue())) {
                throw new IllegalArgumentException("Выберите должность.");
            }
            ViewSupport.readInteger(salaryField, "Зарплата", 0);
        });

        return dialog.showAndWait().filter(button -> button == saveButton).map(button -> new EmolyeeModel(
                create ? 0 : employee.getId(),
                ViewSupport.editedText(nameField, create ? null : employee.getFullName()),
                ViewSupport.editedText(phoneField, create ? null : employee.getPhone()),
                roleField.getValue(),
                ViewSupport.readInteger(salaryField, "Зарплата", 0)));
    }

    private void saveEmployee(EmolyeeModel employee, boolean create) {
        busy.set(true);
        statusLabel.setText("Сохранение сотрудника…");
        ViewSupport.runTask(() -> create ? employeeService.createEmployee(employee)
                : employeeService.updateEmployee(employee) != null, saved -> {
            if (!saved) {
                ViewSupport.showAlert(employeeTable, Alert.AlertType.WARNING,
                        "Запись не найдена", "Сотрудник уже удалён. Список будет обновлён.");
            }
            loadEmployees();
        }, this::showError);
    }

    @FXML
    private void onDeleteEmployeeButtonClick() {
        EmolyeeModel employee = employeeTable.getSelectionModel().getSelectedItem();
        if (employee == null || !ViewSupport.confirmDelete(employeeTable, "сотрудника «" + employee.getFullName() + "»")) {
            return;
        }
        busy.set(true);
        statusLabel.setText("Удаление сотрудника…");
        ViewSupport.runTask(() -> employeeService.deleteEmployeeById(employee.getId()) != null, deleted -> {
            if (!deleted) {
                ViewSupport.showAlert(employeeTable, Alert.AlertType.WARNING, "Запись не найдена", "Сотрудник уже удалён.");
            }
            loadEmployees();
        }, this::showError);
    }

    @FXML
    private void onRefreshEmployeeButtonClick() {
        loadEmployees();
    }

    @FXML
    private void onBackButtonClick() {
        ViewSupport.openView(employeeTable, "hello-view.fxml");
    }

    private void showError(Throwable exception) {
        busy.set(false);
        statusLabel.setText("Операция не выполнена. Попробуйте обновить список.");
        ViewSupport.showDatabaseError(employeeTable, exception);
    }
}

