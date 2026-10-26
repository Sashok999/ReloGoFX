package ru.mirea.movingcompany.abdykalykov.controllers;

import ru.mirea.movingcompany.abdykalykov.models.ClientModel;
import ru.mirea.movingcompany.abdykalykov.enums.ClientStatus;
import ru.mirea.movingcompany.abdykalykov.services.ClientService;
import ru.mirea.movingcompany.abdykalykov.repositories.JdbcClientRepository;
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

public class ClientController {

    private final ClientService clientService = new ClientService(new JdbcClientRepository());

    @FXML private TableView<ClientModel> clientTable;
    @FXML private TableColumn<ClientModel, Number> idClient;
    @FXML private TableColumn<ClientModel, String> nameClient;
    @FXML private TableColumn<ClientModel, String> phoneClient;
    @FXML private TableColumn<ClientModel, String> emailClient;
    @FXML private TableColumn<ClientModel, String> statusClient;
    @FXML private Button createClientButton;
    @FXML private Button updateClientButton;
    @FXML private Button deleteClientButton;
    @FXML private Button refreshClientButton;
    @FXML private Label statusLabel;

    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        idClient.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getId()));
        nameClient.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));
        phoneClient.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPhone()));
        emailClient.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getEmail()));
        statusClient.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ViewSupport.label(cell.getValue().getStatus())));
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
        ViewSupport.runTask(clientService::getAllClients, clients -> {
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
        ClientModel client = clientTable.getSelectionModel().getSelectedItem();
        if (client != null) {
            showClientDialog(client).ifPresent(updated -> saveClient(updated, false));
        }
    }

    private Optional<ClientModel> showClientDialog(ClientModel client) {
        boolean create = client == null;
        Dialog<ButtonType> dialog = new Dialog<>();
        ViewSupport.prepareDialog(dialog, clientTable);
        dialog.setTitle(create ? "Создание клиента" : "Редактирование клиента");
        dialog.setHeaderText(create ? "Новый клиент" : "Клиент №" + client.getId());
        ButtonType saveButton = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton,
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));

        TextField nameField = new TextField(create ? "" : client.getName());
        TextField phoneField = new TextField(create ? "" : client.getPhone());
        TextField emailField = new TextField(create ? "" : client.getEmail());
        ComboBox<ClientStatus> statusField = new ComboBox<>();
        statusField.getItems().addAll(ClientStatus.values());
        statusField.setValue(create ? ClientStatus.NEW : client.getStatus());
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

        return dialog.showAndWait().filter(button -> button == saveButton).map(button -> new ClientModel(
                create ? 0 : client.getId(),
                ViewSupport.editedText(nameField, create ? null : client.getName()),
                ViewSupport.editedText(phoneField, create ? null : client.getPhone()),
                ViewSupport.editedText(emailField, create ? null : client.getEmail()),
                statusField.getValue()));
    }

    private void saveClient(ClientModel client, boolean create) {
        busy.set(true);
        statusLabel.setText("Сохранение клиента…");
        ViewSupport.runTask(() -> create ? clientService.createClient(client)
                : clientService.updateClient(client) != null, saved -> {
            if (!saved) {
                ViewSupport.showAlert(clientTable, Alert.AlertType.WARNING, "Запись не найдена", "Клиент уже удалён.");
            }
            loadClients();
        }, this::showError);
    }

    @FXML
    private void onDeleteClientButtonClick() {
        ClientModel client = clientTable.getSelectionModel().getSelectedItem();
        if (client == null || !ViewSupport.confirmDelete(clientTable, "клиента «" + client.getName() + "»")) {
            return;
        }
        busy.set(true);
        statusLabel.setText("Удаление клиента…");
        ViewSupport.runTask(() -> clientService.deleteClientById(client.getId()) != null, deleted -> {
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

