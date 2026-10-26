package ru.mirea.movingcompany.controllers;

import ru.mirea.movingcompany.Main;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

// Common window and background-query code for the three controllers.
public class ViewSupport {

    public static void openView(Node source, String file) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(file));
            source.getScene().setRoot(loader.load());
        } catch (IOException exception) {
            exception.printStackTrace();
            showAlert(source, Alert.AlertType.ERROR, "Не удалось открыть экран", exception.getMessage());
        }
    }

    public static <T> void runTask(Callable<T> action, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.call();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> onFailure.accept(task.getException()));
        Thread thread = new Thread(task, "relogo-database");
        thread.setDaemon(true);
        thread.start();
    }

    public static void prepareDialog(Dialog<?> dialog, Node source) {
        if (source.getScene() != null && source.getScene().getWindow() != null) {
            dialog.initOwner(source.getScene().getWindow());
        }
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(Main.class.getResource("style.css")).toExternalForm());
        dialog.setResizable(true);
    }

    public static void showAlert(Node source, Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        prepareDialog(alert, source);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean confirmDelete(Node source, String description) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        prepareDialog(alert, source);
        alert.setTitle("Удаление записи");
        alert.setHeaderText("Удалить " + description + "?");
        alert.setContentText("Это действие нельзя отменить.");
        ButtonType deleteButton = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(deleteButton, new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
        return alert.showAndWait().filter(button -> button == deleteButton).isPresent();
    }

    public static void showDatabaseError(Node source, Throwable exception) {
        exception.printStackTrace();
        String message = "Не удалось выполнить операцию. Проверьте подключение и обновите список.";
        Throwable cause = exception;
        while (!(cause instanceof SQLException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof SQLException sqlException) {
            String state = sqlException.getSQLState();
            if ("23503".equals(state)) {
                message = "Проверьте связи между записями и обновите список. "
                        + "Перед удалением клиента или сотрудника измените или удалите связанные заказы.";
            } else if ("23505".equals(state)) {
                message = "Запись с таким ID уже существует. Если данные добавлялись вручную, "
                        + "синхронизируйте счётчик ID таблицы в PostgreSQL.";
            } else if ("28P01".equals(state)) {
                message = "Неверное имя пользователя или пароль PostgreSQL.";
            } else if ("3D000".equals(state)) {
                message = "База данных не найдена. Проверьте название базы в DB_URL.";
            } else if (sqlException.getMessage() != null && sqlException.getMessage().startsWith("Задайте DB_")) {
                message = sqlException.getMessage();
            }
        } else if (cause instanceof IllegalArgumentException) {
            message = cause.getMessage();
        }
        showAlert(source, Alert.AlertType.ERROR, "Ошибка базы данных", message);
    }

    public static void validateDialog(Dialog<?> dialog, ButtonType saveButton, Runnable validation) {
        dialog.getDialogPane().lookupButton(saveButton).addEventFilter(ActionEvent.ACTION, event -> {
            try {
                validation.run();
            } catch (IllegalArgumentException exception) {
                event.consume();
                showAlert(dialog.getDialogPane(), Alert.AlertType.WARNING, "Проверьте данные", exception.getMessage());
            }
        });
    }

    public static void requireText(String text, String field) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Заполните поле «" + field + "».");
        }
    }

    public static int readInteger(TextField field, String name, int minimum) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value < minimum) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Поле «" + name + "»: введите целое число не меньше " + minimum + ".");
        }
    }

    public static String editedText(TextField field, String original) {
        String text = field.getText();
        return Objects.equals(text, original == null ? "" : original) ? original : text.trim();
    }

    public static <T> void setChoiceLabels(ComboBox<T> choice, Function<T, String> label) {
        choice.setMaxWidth(Double.MAX_VALUE);
        choice.setConverter(new StringConverter<>() {
            @Override
            public String toString(T value) {
                return value == null ? "" : label.apply(value);
            }

            @Override
            public T fromString(String value) {
                throw new UnsupportedOperationException("Выберите значение из списка.");
            }
        });
    }

    public static String label(Enum<?> value) {
        return label(value == null ? (String) null : value.name());
    }

    public static String label(String value) {
        if (value == null) {
            return "—";
        }
        return switch (value) {
            case "LOADER" -> "Грузчик";
            case "DRIVER" -> "Водитель";
            case "FOREMAN" -> "Бригадир";
            case "MANAGER" -> "Менеджер";
            case "NEW" -> "Новый";
            case "ACTIVE" -> "Активный";
            case "REGULAR" -> "Постоянный";
            case "BLOCKED" -> "Заблокирован";
            case "IN_PROCESSING" -> "В обработке";
            case "IN_PROGRESS" -> "Выполняется";
            case "FINISHED" -> "Завершён";
            default -> value;
        };
    }
}

