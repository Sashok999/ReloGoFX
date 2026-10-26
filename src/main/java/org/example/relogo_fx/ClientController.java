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

public class ClientController {

    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, Number> idClient;
    @FXML private TableColumn<Client, String> nameClient;
    @FXML private TableColumn<Client, String> phoneClient;
    @FXML private TableColumn<Client, String> emailClient;
    @FXML private TableColumn<Client, String> statusClient;
    @FXML private Button createClientButton;
    @FXML private Button updateClientButton;
    @FXML private Button deleteClientButton;
    @FXML private Button refreshClientButton;
    @FXML private Label statusLabel;

    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        idClient.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().id()));
        nameClient.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
        phoneClient.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().phone()));
        emailClient.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().email()));
        statusClient.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ViewSupport.label(cell.getValue().status())));
        clientTable.setPlaceholder(new Label("Клиентов пока нет"));
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        clientTable.disableProperty().bind(busy);
        createClientButton.disableProperty().bind(busy);
        refreshClientButton.disableProperty().bind(busy);
        updateClientButton.disableProperty().bind(busy.or(clientTable.getSelectionModel().selectedItemProperty().isNull()));
        deleteClientButton.disableProperty().bind(updateClientButton.disableProperty());
        loadClients();
    }

    private void loadClients() {
        busy.set(true);
        statusLabel.setText("Загрузка клиентов…");
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return Database.readClient(connection);
            }
        }, clients -> {
            clientTable.getItems().setAll(clients);
            statusLabel.setText("Клиентов: " + clients.size());
            busy.set(false);
        }, this::showError);
    }

    @FXML
    private void onCreateClientButtonClick() {
        showClientDialog(null).ifPresent(client -> saveClient(client, true));
    }

    @FXML
    private void onUpdateClientButtonClick() {
        Client client = clientTable.getSelectionModel().getSelectedItem();
        if (client != null) {
            showClientDialog(client).ifPresent(updated -> saveClient(updated, false));
        }
    }

    private Optional<Client> showClientDialog(Client client) {
        boolean create = client == null;
        Dialog<ButtonType> dialog = new Dialog<>();
        ViewSupport.prepareDialog(dialog, clientTable);
        dialog.setTitle(create ? "Создание клиента" : "Редактирование клиента");
        dialog.setHeaderText(create ? "Новый клиент" : "Клиент №" + client.id());
        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton,
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));

        TextField nameField = new TextField(create ? "" : client.name());
        TextField phoneField = new TextField(create ? "" : client.phone());
        TextField emailField = new TextField(create ? "" : client.email());
        ComboBox<String> statusField = new ComboBox<>();
        statusField.getItems().addAll("NEW", "ACTIVE", "REGULAR", "BLOCKED");
        statusField.setValue(create ? "NEW" : client.status());
        ViewSupport.setChoiceLabels(statusField, ViewSupport::label);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        form.addRow(0, new Label("ФИО:"), nameField);
        form.addRow(1, new Label("Телефон:"), phoneField);
        form.addRow(2, new Label("Email:"), emailField);
        form.addRow(3, new Label("Статус:"), statusField);
        dialog.getDialogPane().setContent(form);

        ViewSupport.validateDialog(dialog, saveButton, () -> {
            ViewSupport.requireText(nameField.getText(), "ФИО");
            if (!statusField.getItems().contains(statusField.getValue())) {
                throw new IllegalArgumentException("Выберите статус клиента.");
            }
        });

        return dialog.showAndWait().filter(button -> button == saveButton).map(button -> new Client(
                create ? 0 : client.id(),
                ViewSupport.editedText(nameField, create ? null : client.name()),
                ViewSupport.editedText(phoneField, create ? null : client.phone()),
                ViewSupport.editedText(emailField, create ? null : client.email()),
                statusField.getValue()));
    }

    private void saveClient(Client client, boolean create) {
        busy.set(true);
        statusLabel.setText("Сохранение клиента…");
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return create ? Database.createClient(connection, client) : Database.updateClient(connection, client);
            }
        }, saved -> {
            if (!saved) {
                ViewSupport.showAlert(clientTable, Alert.AlertType.WARNING, "Запись не найдена", "Клиент уже удалён.");
            }
            loadClients();
        }, this::showError);
    }

    @FXML
    private void onDeleteClientButtonClick() {
        Client client = clientTable.getSelectionModel().getSelectedItem();
        if (client == null || !ViewSupport.confirmDelete(clientTable, "клиента «" + client.name() + "»")) {
            return;
        }
        busy.set(true);
        statusLabel.setText("Удаление клиента…");
        ViewSupport.runTask(() -> {
            try (Connection connection = Database.getConnection()) {
                return Database.deleteClient(connection, client.id());
            }
        }, deleted -> {
            if (!deleted) {
                ViewSupport.showAlert(clientTable, Alert.AlertType.WARNING, "Запись не найдена", "Клиент уже удалён.");
            }
            loadClients();
        }, this::showError);
    }

    @FXML
    private void onRefreshClientButtonClick() {
        loadClients();
    }

    @FXML
    private void onBackButtonClick() {
        ViewSupport.openView(clientTable, "hello-view.fxml");
    }

    private void showError(Throwable exception) {
        busy.set(false);
        statusLabel.setText("Операция не выполнена. Попробуйте обновить список.");
        ViewSupport.showDatabaseError(clientTable, exception);
    }
}
