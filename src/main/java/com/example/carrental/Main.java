//package com.example.carrental;
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//public class Main extends Application {
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("/com/example/carrental/view/login.fxml"));
//        primaryStage.setTitle("Car Rental System");
//        primaryStage.setScene(new Scene(root, 800, 600));
//        primaryStage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
package com.example.carrental;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // تحميل واجهة تسجيل الدخول
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/view/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);

            // تحميل ملف CSS
            scene.getStylesheets().add(getClass().getResource("/com/example/carrental/view/styles/style.css").toExternalForm());

            primaryStage.setTitle("Car Rental System");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to load FXML: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // تهيئة قاعدة البيانات
        com.example.carrental.database.DatabaseConnection.getInstance();
        launch(args);
        //rahaf
    }
}