package com.retailpos.controller;

import com.retailpos.MainApp;
import com.retailpos.dao.UserDAO;
import com.retailpos.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            MainApp.setCurrentUser(user);
            try { MainApp.showMain(); }
            catch (Exception e) { e.printStackTrace(); }
        } else {
            showError("Invalid credentials. Please try again.");
            passwordField.clear();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
