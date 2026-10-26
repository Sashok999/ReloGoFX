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
import java.util.List;
import java.util.Optional;

public class MovingOrderController {

    @FXML private TableView<MovingOrder> movingOrderTable;
    @FXML private TableColumn<MovingOrder, Number> idMovingOrder;
    @FXML private TableColumn<MovingOrder, String> clientMovingOrder;
    @FXML private TableColumn<MovingOrder, String> foremanMovingOrder;
    @FXML private TableColumn<MovingOrder, Number> massMovingOrder;
    @FXML private TableColumn<MovingOrder, String> startAddressMovingOrder;
    @FXML private TableColumn<MovingOrder, String> endAddressMovingOrder;
    @FXML private TableColumn<MovingOrder, String> statusMovingOrder;
    @FXML private Button createMovingOrderButton;
    @FXML private Button updateMovingOrderButton;
    @FXML private Button deleteMovingOrderButton;
    @FXML private Button refreshMovingOrderButton;
    @FXML private Label statusLabel;

    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private List<Client> clients = List.of();
    private List<Employee> employees = List.of();

    @FXML
    private void initialize() {
        idMovingOrder.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().id()));
        clientMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(clientName(cell.getValue().idClient())));
        foremanMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(employeeName(cell.getValue().idForeman())));
        massMovingOrder.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().mass()));
        startAddressMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().startAddress()));
        endAddressMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().endAddress()));
        statusMovingOrder.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ViewSupport.label(cell.getValue().status())));
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
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return new OrderData(Database.readMovingOrder(connection), Database.readClient(connection),
                        Database.readEmployee(connection));
            }
        }, data -> {
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
        MovingOrder order = movingOrderTable.getSelectionModel().getSelectedItem();
        if (order != null) {
            showMovingOrderDialog(order).ifPresent(updated -> saveMovingOrder(updated, false));
        }
    }

    private Optional<MovingOrder> showMovingOrderDialog(MovingOrder order) {
        List<Employee> foremen = employees.stream().filter(employee -> "FOREMAN".equals(employee.role())).toList();
        if (clients.isEmpty() || foremen.isEmpty()) {
            ViewSupport.showAlert(movingOrderTable, Alert.AlertType.INFORMATION, "Нужны клиент и бригадир",
                    "Сначала добавьте клиента и сотрудника с должностью «Бригадир», затем обновите список заказов.");
            return Optional.empty();
        }
        boolean create = order == null;
        Dialog<ButtonType> dialog = new Dialog<>();
        ViewSupport.prepareDialog(dialog, movingOrderTable);
        dialog.setTitle(create ? "Создание заказа" : "Редактирование заказа");
        dialog.setHeaderText(create ? "Новый переезд" : "Заказ №" + order.id());
        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton,
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));

        ComboBox<Client> clientField = new ComboBox<>();
        clientField.getItems().setAll(clients);
        clientField.setPromptText("Выберите клиента");
        ViewSupport.setChoiceLabels(clientField, client -> "№" + client.id() + " — " + client.name());
        ComboBox<Employee> foremanField = new ComboBox<>();
        foremanField.getItems().setAll(foremen);
        foremanField.setPromptText("Выберите бригадира");
        ViewSupport.setChoiceLabels(foremanField, employee -> "№" + employee.id() + " — " + employee.fullName());
        if (!create) {
            clientField.setValue(clients.stream().filter(client -> client.id() == order.idClient()).findFirst().orElse(null));
            foremanField.setValue(foremen.stream().filter(employee -> employee.id() == order.idForeman()).findFirst().orElse(null));
        }
        TextField massField = new TextField(create ? "" : Integer.toString(order.mass()));
        massField.setPromptText("В килограммах, целое число");
        TextField startAddressField = new TextField(create ? "" : order.startAddress());
        TextField endAddressField = new TextField(create ? "" : order.endAddress());
        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("IN_PROCESSING", "IN_PROGRESS", "FINISHED");
        statusField.setValue(create ? "IN_PROCESSING" : order.status());
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

        return dialog.showAndWait().filter(button -> button == saveButton).map(button -> new MovingOrder(
                create ? 0 : order.id(), clientField.getValue().id(), foremanField.getValue().id(),
                ViewSupport.readInteger(massField, "Масса", 1),
                ViewSupport.editedText(startAddressField, create ? null : order.startAddress()),
                ViewSupport.editedText(endAddressField, create ? null : order.endAddress()), statusField.getValue()));
    }

    private void saveMovingOrder(MovingOrder order, boolean create) {
        busy.set(true);
        statusLabel.setText("Сохранение заказа…");
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return create ? Database.createMovingOrder(connection, order) : Database.updateMovingOrder(connection, order);
            }
        }, saved -> {
            if (!saved) {
                ViewSupport.showAlert(movingOrderTable, Alert.AlertType.WARNING, "Запись не найдена", "Заказ уже удалён.");
            }
            loadMovingOrders();
        }, this::showError);
    }

    @FXML
    private void onDeleteMovingOrderButtonClick() {
        MovingOrder order = movingOrderTable.getSelectionModel().getSelectedItem();
        if (order == null || !ViewSupport.confirmDelete(movingOrderTable, "заказ №" + order.id())) {
            return;
        }
        busy.set(true);
        statusLabel.setText("Удаление заказа…");
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return Database.deleteMovingOrder(connection, order.id());
            }
        }, deleted -> {
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
        return clients.stream().filter(client -> client.id() == id).findFirst()
                .map(client -> "№" + id + " · " + client.name()).orElse("№" + id);
    }

    private String employeeName(int id) {
        return employees.stream().filter(employee -> employee.id() == id).findFirst()
                .map(employee -> "№" + id + " · " + employee.fullName()).orElse("№" + id);
    }

    private void showError(Throwable exception) {
        busy.set(false);
        statusLabel.setText("Операция не выполнена. Попробуйте обновить список.");
        ViewSupport.showDatabaseError(movingOrderTable, exception);
    }

    private record OrderData(List<MovingOrder> orders, List<Client> clients, List<Employee> employees) {}
}
