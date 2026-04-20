package com.retailpos.controller;

import com.retailpos.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML private StackPane contentArea;
    @FXML private Label userLabel;
    @FXML private Button posBtn, inventoryBtn, returnsBtn, usersBtn, reportsBtn;

    @FXML
    public void initialize() {
        userLabel.setText(MainApp.getCurrentUser().getName() + "  |  " + MainApp.getCurrentUser().getRole());
        // Hide admin-only buttons for cashiers
        if (!MainApp.getCurrentUser().isAdmin()) {
            usersBtn.setVisible(false);
        }
        loadView("/fxml/POS.fxml");
    }

    @FXML private void showPOS() { loadView("/fxml/POS.fxml"); setActive(posBtn); }
    @FXML private void showInventory() { loadView("/fxml/Inventory.fxml"); setActive(inventoryBtn); }
    @FXML private void showReturns() { loadView("/fxml/Returns.fxml"); setActive(returnsBtn); }
    @FXML private void showUsers() {
        if (MainApp.getCurrentUser().isAdmin()) { loadView("/fxml/Users.fxml"); setActive(usersBtn); }
    }
    @FXML private void showReports() { loadView("/fxml/Reports.fxml"); setActive(reportsBtn); }

    @FXML
    private void handleLogout() {
        MainApp.setCurrentUser(null);
        try { MainApp.showLogin(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setActive(Button active) {
        for (Button btn : new Button[]{posBtn, inventoryBtn, returnsBtn, usersBtn, reportsBtn}) {
            if (btn != null) btn.getStyleClass().remove("nav-active");
        }
        active.getStyleClass().add("nav-active");
    }
}
