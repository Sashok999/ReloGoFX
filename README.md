# ReloGo FX

Приложение для управления компанией по переездам.

**Стек:** Java 17+, JavaFX, PostgreSQL, Maven.

**Возможности:** просмотр, создание, редактирование и удаление сотрудников, клиентов и заказов.

## Запуск

1. Создайте базу PostgreSQL с таблицами `employees`, `clients`, `moving_orders`.
2. Задайте переменные окружения `DB_URL`, `DB_USER`, `DB_PASSWORD`.
3. Запустите `Launcher.java` или выполните `./mvnw.cmd javafx:run`.
