package org.example.relogo_fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.Connection;
import java.util.Optional;

public class EmployeeController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Number> idEmployee;
    @FXML private TableColumn<Employee, String> fullNameEmployee;
    @FXML private TableColumn<Employee, String> phoneEmployee;
    @FXML private TableColumn<Employee, String> roleEmployee;
    @FXML private TableColumn<Employee, Number> salaryEmployee;
    @FXML private Button createEmployeeButton;
    @FXML private Button updateEmployeeButton;
    @FXML private Button deleteEmployeeButton;
    @FXML private Button refreshEmployeeButton;
    @FXML private Label statusLabel;

    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        idEmployee.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().id()));
        fullNameEmployee.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().fullName()));
        phoneEmployee.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().phone()));
        roleEmployee.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ViewSupport.label(cell.getValue().role())));
        salaryEmployee.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().salary()));
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
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return Database.readEmployee(connection);
            }
        }, employees -> {
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
        Employee employee = employeeTable.getSelectionModel().getSelectedItem();
        if (employee != null) {
            showEmployeeDialog(employee).ifPresent(updated -> saveEmployee(updated, false));
        }
    }

    private Optional<Employee> showEmployeeDialog(Employee employee) {
        boolean create = employee == null;
        Dialog<ButtonType> dialog = new Dialog<>();
        ViewSupport.prepareDialog(dialog, employeeTable);
        dialog.setTitle(create ? "Создание сотрудника" : "Редактирование сотрудника");
        dialog.setHeaderText(create ? "Новый сотрудник" : "Сотрудник №" + employee.id());

        ButtonType saveButton = new ButtonType(
                "Сохранить", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton, new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE)
        );


        TextField nameField = new TextField(create ? "" : employee.fullName());
        TextField phoneField = new TextField(create ? "" : employee.phone());
        ComboBox<String> roleField = new ComboBox<>();
        roleField.getItems().addAll("LOADER", "DRIVER", "FOREMAN", "MANAGER");
        roleField.setValue(create ? "LOADER" : employee.role());
        ViewSupport.setChoiceLabels(roleField, ViewSupport::label);
        TextField salaryField = new TextField(create ? "" : Integer.toString(employee.salary()));
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

        return dialog.showAndWait().filter(button -> button == saveButton).map(button -> new Employee(
                create ? 0 : employee.id(),
                ViewSupport.editedText(nameField, create ? null : employee.fullName()),
                ViewSupport.editedText(phoneField, create ? null : employee.phone()),
                roleField.getValue(),
                ViewSupport.readInteger(salaryField, "Зарплата", 0)));
    }

    private void saveEmployee(Employee employee, boolean create) {
        busy.set(true);
        statusLabel.setText("Сохранение сотрудника…");
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return create ? Database.createEmployee(connection, employee)
                        : Database.updateEmployee(connection, employee);
            }
        }, saved -> {
            if (!saved) {
                ViewSupport.showAlert(employeeTable, Alert.AlertType.WARNING,
                        "Запись не найдена", "Сотрудник уже удалён. Список будет обновлён.");
            }
            loadEmployees();
        }, this::showError);
    }

    @FXML
    private void onDeleteEmployeeButtonClick() {
        Employee employee = employeeTable.getSelectionModel().getSelectedItem();
        if (employee == null || !ViewSupport.confirmDelete(employeeTable, "сотрудника «" + employee.fullName() + "»")) {
            return;
        }
        busy.set(true);
        statusLabel.setText("Удаление сотрудника…");
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return Database.deleteEmployee(connection, employee.id());
            }
        }, deleted -> {
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
