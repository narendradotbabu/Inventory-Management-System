package com.retailpos;

import com.retailpos.model.User;
import com.retailpos.util.DatabaseHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static User currentUser;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        DatabaseHelper.getInstance(); // Initialize DB
        showLogin();
    }

    public static void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(MainApp.class.getResource("/css/styles.css").toExternalForm());
        primaryStage.setTitle("RetailPOS — Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void showMain() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/Main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(MainApp.class.getResource("/css/styles.css").toExternalForm());
        primaryStage.setTitle("RetailPOS — " + currentUser.getName() + " [" + currentUser.getRole() + "]");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User user) { currentUser = user; }
    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) { launch(args); }
}
