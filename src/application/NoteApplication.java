package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class NoteApplication extends Application {
  @Override
  public void start(Stage primaryStage) {
    try {
      Parent root = FXMLLoader.load(getClass().getResource("scene1.fxml"));
      Scene scene = new Scene(root);

      Image appIcon = new Image("images/icon.png");
      primaryStage.getIcons().add(appIcon);

      /*
      String css = this.getClass().getResource("Scene1.css").toExternalForm();
      scene.getStylesheets().add(css);

      String css_theme = this.getClass().getResource("../styles/lightmode.css").toExternalForm();
      scene.getStylesheets().add(css_theme);
      */
      
      primaryStage.setTitle("Skrib");
      primaryStage.setScene(scene);
      primaryStage.show();

    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) {
    launch(args);
  }
}