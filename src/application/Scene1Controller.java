package application;

import java.io.File;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.HTMLEditor;
import javafx.stage.DirectoryChooser;
import javafx.scene.layout.BorderPane;

public class Scene1Controller {
  // Injecting FXML elements to the controller class
  @FXML
  private HTMLEditor htmlEditor; // User makes their notes here

  @FXML
  private TreeView<String> treeView; // Displays names of notes
  
  @FXML
  private ScrollPane treeScrollPane; // Treeview Scroll Pane
  
  @FXML
  private BorderPane borderPane; // Contains the main layout

  @FXML
  private Button toggleTreeViewButton; // Button to toggle the TreeView

  @FXML
  private Label wordCountLabel; // Label to display word count

  private File storageDirectory; // User choice directory
  
  private static final String PREF_KEY_DIRECTORY = "storageDirectoryPath";

  public void initialize() { // Runs when application is loaded
    storageDirectory = getStorageDirectory();
    if (storageDirectory == null) {
      System.err.println("Directory is not selected");
      return;
    } else {
      // Create the root TreeItem using the directory name.
      TreeItem<String> rootItem = new TreeItem<>(storageDirectory.getName());
      treeView.setRoot(rootItem);

      // Recursively populate the TreeView with subdirectories and files.
      populateTreeView(rootItem, storageDirectory);
    }
  }

  // Retrieves directory of user's choice
  private File getStorageDirectory() {
      File storedDir = getStoredDirectory();
      return storedDir;
  }


  // Allow user to select a directory of there choice
  private File selectDirectory() {
    File storedDir = getStoredDirectory(); // Gets the previously stored directory

    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Choose Storage Directory");

    // Address of user's home directory
    String userHome = System.getProperty("user.home");

    /* Handling edge cases where:
        - user has not choosen a directory before
        - chooses directory but deletes it
        - chooses a file rather than directory 
      */
    // Directory which user will be presented to choose their directory from
    File initialDir = (storedDir != null && storedDir.exists() && storedDir.isDirectory())
                        ? storedDir : new File(userHome);
                        
    directoryChooser.setInitialDirectory(initialDir);

    return directoryChooser.showDialog(null);
  }

  public void handleSelectDirectory() {
    File selectedDirectory = selectDirectory();
    if(selectedDirectory != null) {
      storeDirectory(selectedDirectory);
      storageDirectory = selectedDirectory;

      // Update the treeview with new directory
      TreeItem<String> rootItem = new TreeItem<>(storageDirectory.getName());
        treeView.setRoot(rootItem);
        populateTreeView(rootItem, storageDirectory);
    }
  }

  // Populating the TreeView control
  private void populateTreeView(TreeItem<String> parent, File directory) {
      File[] files = directory.listFiles();
      if (files != null) {
          for (File file : files) {
              TreeItem<String> treeItem = new TreeItem<>(file.getName());
              parent.getChildren().add(treeItem);
              if (file.isDirectory()) {
                  populateTreeView(treeItem, file);
              }
          }
      }
  }

  // Retrieves the stored directory path from user's local system
  private File getStoredDirectory() {
    Preferences prefs = Preferences.userNodeForPackage(Controller.class);
    String path = prefs.get(PREF_KEY_DIRECTORY, null);
    if (path != null) {
      File dir = new File(path);
      if (dir.exists() && dir.isDirectory()) { 
        // if directory hasn't been deleted yet
        return dir;
      }
    }
    return null;
  }

  // Store choosen directory into local system of user
  private void storeDirectory(File directory) {
      Preferences prefs = Preferences.userNodeForPackage(Controller.class);
      prefs.put(PREF_KEY_DIRECTORY, directory.getAbsolutePath());
  }

  // Use a key released event to update the word count from the HTMLEditor content
  public void handleClickEventInWebView() {
    updateWordCount(htmlEditor.getHtmlText());
  }

  // handles toggle button to show/hide the TreeView.
  public void handleToggleButtonClick() {

    toggleTreeViewButton.setOnAction(_ -> {
      if (treeView.isVisible()) {
        treeView.setVisible(false); // this will not be shown either way
        borderPane.setLeft(null);  // remove the ScrollPane - treeView container
        toggleTreeViewButton.setText("Show Notes List");
      } else {
        borderPane.setLeft(treeScrollPane); // adds the ScrollPane back
        treeView.setVisible(true); // treeview is made visible again
        toggleTreeViewButton.setText("Hide Notes List");
      }
    });
  }

  // Converts html to normal text and counts the words
  private void updateWordCount(String htmlText) {
    String text = htmlEditor.getHtmlText(); // extracts text from htmlEditor

    text = text.replaceAll("(?i)<br\\s*/?>", "\n"); // replace linebreaks 
    text = text.replaceAll("(?i)</p>", "\n");

    // remove all remaining HTML tags.
    String plainText = text.replaceAll("<[^>]+>", "");

    // handle text that is split across multiple lines by space, newline tab etc
    String[] words = plainText.trim().split("\\s+");

    // calculates number of words
    int wordCount = plainText.trim().isEmpty() ? 0 : words.length;

    // Update the word count label
    if (wordCountLabel != null) {
        if(wordCount > 1) 
          wordCountLabel.setText(wordCount + " Words");
        else if(wordCount == 0)
          wordCountLabel.setText("0 Word");
        else
        wordCountLabel.setText("1 Word");
        System.out.println(wordCount);
    } else {
        System.err.println("Reload");
    }
  }
}