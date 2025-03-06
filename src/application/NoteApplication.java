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
      // Load FXML file for the main scene
      FXMLLoader loader = new FXMLLoader(getClass().getResource("scene1.fxml"));
      Parent root = loader.load();
      Scene scene = new Scene(root);

      // Set application icon
      Image appIcon = new Image("images/icon.png");
      primaryStage.getIcons().add(appIcon);

      // Load the CSS file
      String css = this.getClass().getResource("/styles/lightmode.css").toExternalForm();
      scene.getStylesheets().add(css);

      Scene1Controller controller = loader.getController();
      controller.initializeZoomHandlers((scene));

      // Title of the primary stage
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