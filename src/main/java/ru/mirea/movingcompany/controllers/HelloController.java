package ru.mirea.movingcompany.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class HelloController {
    @FXML
    private void onEmployeeButtonClick(ActionEvent event) {
        ViewSupport.openView((Node) event.getSource(), "employee-view.fxml");
    }

    @FXML
    private void onClientButtonClick(ActionEvent event) {
        ViewSupport.openView((Node) event.getSource(), "client-view.fxml");
    }

    @FXML
    private void onMovingOrderButtonClick(ActionEvent event) {
        ViewSupport.openView((Node) event.getSource(), "moving-order-view.fxml");
    }
}

