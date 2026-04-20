package com.retailpos.controller;

import com.retailpos.MainApp;
import com.retailpos.dao.UserDAO;
import com.retailpos.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class UsersController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;
    @FXML private Label statusLabel;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        enforceAccess();
        loadUsers();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        userTable.setItems(users);
        userTable.setPlaceholder(new Label("No users available."));
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            boolean hasSelection = newUser != null;
            boolean admin = MainApp.getCurrentUser() != null && MainApp.getCurrentUser().isAdmin();
            editBtn.setDisable(!admin || !hasSelection);
            deleteBtn.setDisable(!admin || !hasSelection);
        });
    }

    private void enforceAccess() {
        boolean admin = MainApp.getCurrentUser() != null && MainApp.getCurrentUser().isAdmin();
        addBtn.setDisable(!admin);
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
        if (!admin) showStatus("Only admins can manage users.", true);
    }

    private void loadUsers() {
        users.setAll(userDAO.getAllUsers());
        if (users.isEmpty()) showStatus("No users found.", true);
        else if (statusLabel.getText() == null || statusLabel.getText().isBlank()) showStatus("Loaded " + users.size() + " users.", false);
    }

    @FXML
    private void handleAdd() {
        if (!ensureAdmin()) return;
        showUserDialog(null).ifPresent(user -> {
            if (userDAO.usernameExists(user.getUsername(), null)) {
                showStatus("Username already exists. Please choose another one.", true);
                return;
            }
            if (userDAO.addUser(user)) {
                loadUsers();
                showStatus("User added successfully.", false);
            } else {
                showStatus("Failed to add user. Username may already exist.", true);
            }
        });
    }

    @FXML
    private void handleEdit() {
        if (!ensureAdmin()) return;
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a user to edit.", true);
            return;
        }

        showUserDialog(selected).ifPresent(user -> {
            if (userDAO.usernameExists(user.getUsername(), selected.getId())) {
                showStatus("Username already exists. Please choose another one.", true);
                return;
            }
            if (userDAO.updateUser(user)) {
                loadUsers();
                showStatus("User updated successfully.", false);
            } else {
                showStatus("Failed to update user.", true);
            }
        });
    }

    @FXML
    private void handleDelete() {
        if (!ensureAdmin()) return;
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a user to delete.", true);
            return;
        }
        if (selected.getId() == MainApp.getCurrentUser().getId()) {
            showStatus("You cannot delete the currently logged-in admin.", true);
            return;
        }
        if (userDAO.hasSales(selected.getId())) {
            showStatus("This user cannot be deleted because sales are linked to the account.", true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete user " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (userDAO.deleteUser(selected.getId())) {
                loadUsers();
                showStatus("User deleted.", false);
            } else {
                showStatus("Failed to delete user.", true);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        showStatus("Users refreshed.", false);
    }

    private Optional<User> showUserDialog(User existing) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add User" : "Edit User");
        dialog.setHeaderText(existing == null ? "Create a new user account." : "Update user details.");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        TextField usernameField = new TextField(existing != null ? existing.getUsername() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(existing != null ? existing.getPassword() : "");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "CASHIER");
        roleCombo.setValue(existing != null ? existing.getRole() : "CASHIER");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;

            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleCombo.getValue();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
                showStatus("All user fields are required.", true);
                return null;
            }

            return existing != null
                ? new User(existing.getId(), name, username, password, role)
                : new User(0, name, username, password, role);
        });
        return dialog.showAndWait();
    }

    private boolean ensureAdmin() {
        if (MainApp.getCurrentUser() != null && MainApp.getCurrentUser().isAdmin()) return true;
        showStatus("Only admins can manage users.", true);
        return false;
    }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
