package ru.mirea.movingcompany.belov.controllers;

import ru.mirea.movingcompany.belov.models.MovingOrderModel;
import ru.mirea.movingcompany.belov.enums.MovingOrderStatus;
import ru.mirea.movingcompany.abdykalykov.models.ClientModel;
import ru.mirea.movingcompany.abdykalykov.services.ClientService;
import ru.mirea.movingcompany.abdykalykov.repositories.JdbcClientRepository;
import ru.mirea.movingcompany.gall.models.EmolyeeModel;
import ru.mirea.movingcompany.gall.enums.EmployeeRoles;
import ru.mirea.movingcompany.gall.services.EmployeeService;
import ru.mirea.movingcompany.gall.repositories.JdbcEmployeeRepository;
import ru.mirea.movingcompany.belov.services.MovingOrderService;
import ru.mirea.movingcompany.belov.repositories.JdbcMovingOrderRepository;
import ru.mirea.movingcompany.controllers.ViewSupport;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

public class MovingOrderController {

    private final MovingOrderService movingOrderService = new MovingOrderService(new JdbcMovingOrderRepository());
    private final ClientService clientService = new ClientService(new JdbcClientRepository());
    private final EmployeeService employeeService = new EmployeeService(new JdbcEmployeeRepository());

    @FXML private TableView<MovingOrderModel> movingOrderTable;
    @FXML private TableColumn<MovingOrderModel, Number> idMovingOrder;
    @FXML private TableColumn<MovingOrderModel, String> clientMovingOrder;
    @FXML private TableColumn<MovingOrderModel, String> foremanMovingOrder;
    @FXML private TableColumn<MovingOrderModel, Number> massMovingOrder;
    @FXML private TableColumn<MovingOrderModel, String> startAddressMovingOrder;
    @FXML private TableColumn<MovingOrderModel, String> endAddressMovingOrder;
    @FXML private TableColumn<MovingOrderModel, String> statusMovingOrder;
    @FXML private Button createMovingOrderButton;
    @FXML private Button updateMovingOrderButton;
    @FXML private Button deleteMovingOrderButton;
    @FXML private Button refreshMovingOrderButton;
    @FXML private Label statusLabel;

    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private List<ClientModel> clients = List.of();
    private List<EmolyeeModel> employees = List.of();

    @FXML
    private void initialize() {
        idMovingOrder.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getId()));
        clientMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(clientName(cell.getValue().getIdClient())));
        foremanMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(employeeName(cell.getValue().getIdForeman())));
        massMovingOrder.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getMass()));
        startAddressMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStartAddress()));
        endAddressMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getEndAddress()));
        statusMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ViewSupport.label(cell.getValue().getStatus())));
        movingOrderTable.setPlaceholder(new Label("Заказов пока нет"));
        movingOrderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        movingOrderTable.disableProperty().bind(busy);
        createMovingOrderButton.disableProperty().bind(busy);
        refreshMovingOrderButton.disableProperty().bind(busy);
        updateMovingOrderButton.disableProperty().bind(busy.or(movingOrderTable.getSelectionModel().selectedItemProperty().isNull()));
        deleteMovingOrderButton.disableProperty().bind(updateMovingOrderButton.disableProperty());
        loadMovingOrders();
    }

    private void loadMovingOrders() {
        busy.set(true);
        statusLabel.setText("Загрузка заказов…");
        ViewSupport.runTask(() -> new OrderData(movingOrderService.getAllMovingOrder(), clientService.getAllClients(), employeeService.getAllEmployees()), data -> {
            clients = data.clients();
            employees = data.employees();
            movingOrderTable.getItems().setAll(data.orders());
            movingOrderTable.refresh();
            statusLabel.setText("Заказов: " + data.orders().size());
            busy.set(false);
        }, this::showError);
    }

    @FXML
    private void onCreateMovingOrderButtonClick() {
        showMovingOrderDialog(null).ifPresent(order -> saveMovingOrder(order, true));
    }

    @FXML
    private void onUpdateMovingOrderButtonClick() {
        MovingOrderModel order = movingOrderTable.getSelectionModel().getSelectedItem();
        if (order != null) {
            showMovingOrderDialog(order).ifPresent(updated -> saveMovingOrder(updated, false));
        }
    }

    private Optional<MovingOrderModel> showMovingOrderDialog(MovingOrderModel order) {
        List<EmolyeeModel> foremen = employees.stream().filter(employee -> employee.getRole() == EmployeeRoles.FOREMAN).toList();
        if (clients.isEmpty() || foremen.isEmpty()) {
            ViewSupport.showAlert(movingOrderTable, Alert.AlertType.INFORMATION, "Нужны клиент и бригадир",
                    "Сначала добавьте клиента и сотрудника с должностью «Бригадир», затем обновите список заказов.");
            return Optional.empty();
        }
        boolean create = order == null;
        Dialog<ButtonType> dialog = new Dialog<>();
        ViewSupport.prepareDialog(dialog, movingOrderTable);
        dialog.setTitle(create ? "Создание заказа" : "Редактирование заказа");
        dialog.setHeaderText(create ? "Новый переезд" : "Заказ №" + order.getId());
        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton,
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));

        ComboBox<ClientModel> clientField = new ComboBox<>();
        clientField.getItems().setAll(clients);
        clientField.setPromptText("Выберите клиента");
        ViewSupport.setChoiceLabels(clientField, client -> "№" + client.getId() + " — " + client.getName());
        ComboBox<EmolyeeModel> foremanField = new ComboBox<>();
        foremanField.getItems().setAll(foremen);
        foremanField.setPromptText("Выберите бригадира");
        ViewSupport.setChoiceLabels(foremanField, employee -> "№" + employee.getId() + " — " + employee.getFullName());
        if (!create) {
            clientField.setValue(clients.stream().filter(client -> client.getId() == order.getIdClient()).findFirst().orElse(null));
            foremanField.setValue(foremen.stream().filter(employee -> employee.getId() == order.getIdForeman()).findFirst().orElse(null));
        }
        TextField massField = new TextField(create ? "" : Integer.toString(order.getMass()));
        massField.setPromptText("В килограммах, целое число");
        TextField startAddressField = new TextField(create ? "" : order.getStartAddress());
        TextField endAddressField = new TextField(create ? "" : order.getEndAddress());
        ComboBox<MovingOrderStatus> statusField = new ComboBox<>();
        statusField.getItems().addAll(MovingOrderStatus.values());
        statusField.setValue(create ? MovingOrderStatus.IN_PROCESSING : order.getStatus());
        ViewSupport.setChoiceLabels(statusField, ViewSupport::label);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        form.addRow(0, new Label("Клиент:"), clientField);
        form.addRow(1, new Label("Бригадир:"), foremanField);
        form.addRow(2, new Label("Масса, кг:"), massField);
        form.addRow(3, new Label("Откуда:"), startAddressField);
        form.addRow(4, new Label("Куда:"), endAddressField);
        form.addRow(5, new Label("Статус:"), statusField);
        dialog.getDialogPane().setContent(form);

        ViewSupport.validateDialog(dialog, saveButton, () -> {
            if (clientField.getValue() == null || foremanField.getValue() == null) {
                throw new IllegalArgumentException("Выберите клиента и бригадира.");
            }
            ViewSupport.readInteger(massField, "Масса", 1);
            ViewSupport.requireText(startAddressField.getText(), "Откуда");
            ViewSupport.requireText(endAddressField.getText(), "Куда");
            if (!statusField.getItems().contains(statusField.getValue())) {
                throw new IllegalArgumentException("Выберите статус заказа.");
            }
        });

        return dialog.showAndWait().filter(button -> button == saveButton).map(button -> new MovingOrderModel(
                create ? 0 : order.getId(), clientField.getValue().getId(), foremanField.getValue().getId(),
                ViewSupport.readInteger(massField, "Масса", 1),
                ViewSupport.editedText(startAddressField, create ? null : order.getStartAddress()),
                ViewSupport.editedText(endAddressField, create ? null : order.getEndAddress()), statusField.getValue()));
    }

    private void saveMovingOrder(MovingOrderModel order, boolean create) {
        busy.set(true);
        statusLabel.setText("Сохранение заказа…");
        ViewSupport.runTask(() -> create ? movingOrderService.createMovingOrder(order)
                : movingOrderService.updateMovingOrder(order) != null, saved -> {
            if (!saved) {
                ViewSupport.showAlert(movingOrderTable, Alert.AlertType.WARNING, "Запись не найдена", "Заказ уже удалён.");
            }
            loadMovingOrders();
        }, this::showError);
    }

    @FXML
    private void onDeleteMovingOrderButtonClick() {
        MovingOrderModel order = movingOrderTable.getSelectionModel().getSelectedItem();
        if (order == null || !ViewSupport.confirmDelete(movingOrderTable, "заказ №" + order.getId())) {
            return;
        }
        busy.set(true);
        statusLabel.setText("Удаление заказа…");
        ViewSupport.runTask(() -> movingOrderService.deleteMovingOrderById(order.getId()) != null, deleted -> {
            if (!deleted) {
                ViewSupport.showAlert(movingOrderTable, Alert.AlertType.WARNING, "Запись не найдена", "Заказ уже удалён.");
            }
            loadMovingOrders();
        }, this::showError);
    }

    @FXML
    private void onRefreshMovingOrderButtonClick() {
        loadMovingOrders();
    }

    @FXML
    private void onBackButtonClick() {
        ViewSupport.openView(movingOrderTable, "hello-view.fxml");
    }

    private String clientName(int id) {
        return clients.stream().filter(client -> client.getId() == id).findFirst()
                .map(client -> "№" + id + " · " + client.getName()).orElse("№" + id);
    }

    private String employeeName(int id) {
        return employees.stream().filter(employee -> employee.getId() == id).findFirst()
                .map(employee -> "№" + id + " · " + employee.getFullName()).orElse("№" + id);
    }

    private void showError(Throwable exception) {
        busy.set(false);
        statusLabel.setText("Операция не выполнена. Попробуйте обновить список.");
        ViewSupport.showDatabaseError(movingOrderTable, exception);
    }

    private record OrderData(List<MovingOrderModel> orders, List<ClientModel> clients, List<EmolyeeModel> employees) {}
}

