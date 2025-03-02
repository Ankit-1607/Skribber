package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.web.HTMLEditor;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
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

  @FXML
  private CheckMenuItem autosaveMenuItem; // Menu item to toggle autosave feature

  private File storageDirectory; // User choice directory
  private File currentFile; // Reference to currently opened file

  private static final String PREF_KEY_DIRECTORY = "storageDirectoryPath"; // Key for accessing user's local storage for previously chosen directory

  
  public void initialize() { // Runs when application is loaded
    storageDirectory = getStorageDirectory();
    if (storageDirectory == null) {
      System.err.println("Directory is not selected");
      return;
    } else {
      // create the root TreeItem using the directory name.
      TreeItem<String> rootItem = new TreeItem<>(storageDirectory.getName());
      treeView.setRoot(rootItem);

      // recursively populate the TreeView with subdirectories and files.
      populateTreeView(rootItem, storageDirectory);
    }

    htmlEditor.setVisible(false); // hide the HTMLEditor initially

    // add a listener to the TreeView to handle file selection
    treeView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> { // observable - 1st param, oldValue - 2nd param
        if (newValue != null) {
            TreeItem<String> selectedItem = newValue;
            File selectedFile = new File(storageDirectory, getFilePath(selectedItem));
            if (selectedFile.isFile()) {
                loadFileContent(selectedFile);
            }
        }
    });

    // add a listener to the CheckMenuItem to enable or disable autosave feature
    autosaveMenuItem.selectedProperty().addListener((_, _, newValue) -> {
        if (newValue) {
            enableAutosave();
        } else {
            disableAutosave();
        }
    });
  }

  //       ----------------------------- EVENT HANDLERS ----------------------------- 

  // Event handlers for the various actions that the user can perform in the application - connected to the FXML file

  // User wants to select a directory of their choice
  public void handleSelectDirectory() {
    File selectedDirectory = selectDirectory();
    if(selectedDirectory != null) {
      storeDirectory(selectedDirectory);
      storageDirectory = selectedDirectory;

      // update the treeview with new directory
      TreeItem<String> rootItem = new TreeItem<>(storageDirectory.getName());
      treeView.setRoot(rootItem);
      populateTreeView(rootItem, storageDirectory);
    }
  }

  // Save changes made to the file
  public void saveFileContent() {
    if (currentFile != null) {
      try {
        String content = htmlEditor.getHtmlText();
        Files.write(currentFile.toPath(), content.getBytes());
        // display a confirmation alert to the user
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("File Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your file has been saved successfully.");
        alert.showAndWait(); // shows the stage object and blocks(stays inside showAndWait) until the user closes it
      } catch (IOException e) {
        e.printStackTrace();
        // display an error alert to the user
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Save Error");
        alert.setHeaderText(null);
        alert.setContentText("An error occurred while saving the file.");
        alert.showAndWait();
      }
    } else {
      // display an error alert to the user if no file is currently opened
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No File Opened");
      alert.setHeaderText(null);
      alert.setContentText("No file is currently opened to save.");
      alert.showAndWait();
    }
  }

  // Method to create a new file in the selected folder
  public void createNewFile() {
    TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      File selectedDirectory = new File(storageDirectory, getFilePath(selectedItem));
      if (selectedDirectory.isDirectory()) {
        boolean validName = false;
        while (!validName) { // run this till user enters a valid name
          // create a TextInputDialog to prompt the user for the file name
          TextInputDialog dialog = new TextInputDialog("New File");
          dialog.setTitle("Create New File");
          dialog.setHeaderText("Enter the name of the new file:");
          dialog.setContentText("File name:");

          // show the dialog and wait for the user to provide a file name
          Optional<String> result = dialog.showAndWait();
          if (result.isPresent()) {
            String fileName = result.get();
            // ensure the file name ends with .html - default type for application
            if (!fileName.toLowerCase().endsWith(".html")) {
              fileName += ".html";
            }

            File newFile = new File(selectedDirectory, fileName);
            if (newFile.exists()) {
              // display an error alert if the file already exists
              Alert alert = new Alert(AlertType.ERROR);
              alert.setTitle("File Creation Error");
              alert.setHeaderText(null);
              alert.setContentText("A file with the same name already exists. Please enter a different name.");
              alert.showAndWait();
            } else {
              try {
                if (newFile.createNewFile()) {
                  // add the new file to the TreeView
                  TreeItem<String> newFileItem = new TreeItem<>(newFile.getName());
                  selectedItem.getChildren().add(newFileItem);
                  selectedItem.setExpanded(true);
                  // open the new file in the HTMLEditor
                  loadFileContent(newFile);
                  validName = true;
                } else {
                  // display an error alert if the file could not be created
                  Alert alert = new Alert(AlertType.ERROR);
                  alert.setTitle("File Creation Error");
                  alert.setHeaderText(null);
                  alert.setContentText("The file could not be created.");
                  alert.showAndWait();
                }
              } catch (IOException e) {
                e.printStackTrace();
                // display an error alert if an exception occurs
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("File Creation Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred while creating the file.");
                alert.showAndWait();
              }
            }
          } else {
            // user cancelled the dialog
            break;
          }
        }
      } else {
        // display an error alert if the selected item is not a directory
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Invalid Selection");
        alert.setHeaderText(null);
        alert.setContentText("Please select a directory to create a new file.");
        alert.showAndWait();
      }
    } else {
      // display an error alert if no item is selected
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No Selection");
      alert.setHeaderText(null);
      alert.setContentText("Please select a directory to create a new file.");
      alert.showAndWait();
    }
  }

  // Use a key released event to update the word count from the HTMLEditor content
  public void handleClickEventInWebView() {
    updateWordCount(htmlEditor.getHtmlText());
  }

  // Handles toggle button to show/hide the TreeView.
  public void handleToggleButtonClick() {
    toggleTreeViewButton.setOnAction(_ -> {
      if (treeView.isVisible()) {
        treeView.setVisible(false); // this will not be shown either way since we are removing scroll pane from the borderpane
        borderPane.setLeft(null);  // remove the ScrollPane - treeView container
        toggleTreeViewButton.setText("Show Notes List");
      } else {
        borderPane.setLeft(treeScrollPane); // adds the ScrollPane back
        treeView.setVisible(true); // treeview is made visible again
        toggleTreeViewButton.setText("Hide Notes List");
      }
    });
  }

  // Function to close the currently opened file
  public void closeCurrentFile() {
    String currentContent = htmlEditor.getHtmlText();
    try {
      String fileContent = new String(Files.readAllBytes(currentFile.toPath()));
      if(!currentContent.equals(fileContent)) {
        // prompt the user to save changes
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Found unsaved changes");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to save changes to: " + currentFile.getName() + "?");

        ButtonType buttonTypeYes =  new ButtonType("Yes");
        ButtonType buttonTypeNo =  new ButtonType("No");
        ButtonType buttonTypeCancel =  new ButtonType("Cancel");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()) {
          if(result.get() == buttonTypeYes) {
            saveFileContent();
            clearEditor();
          } else if(result.get() == buttonTypeNo) {
            clearEditor();
          } else {
            // user cancels the operation, Do nothing
            return;
          }
        }
      } else {
        // no changes made to the file, clear the editor
        clearEditor();
      }
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  // Function to delete the currently selected file
  public void deleteSelectedFile() {
    TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if(selectedItem != null) {
      File selectedFile = new File(storageDirectory, getFilePath(selectedItem));
      if(selectedFile.isFile()) {
        // confirm deletion with the user
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete selected Skrib");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete: " + selectedFile.getName() + "?");

        ButtonType buttonTypeYes =  new ButtonType("Yes");
        ButtonType buttonTypeNo =  new ButtonType("No");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == buttonTypeYes) {
          if(selectedFile.delete()) {
            // remove the file from the Treeview
            TreeItem<String> parent = selectedItem.getParent();
            if(parent != null) {
              parent.getChildren().remove(selectedItem);
            } else {
              treeView.setRoot(null);
            }

            // clear the editor if the deleted file is the currently opened file
            if(selectedFile.equals(currentFile)) {
              clearEditor();
            }

            // show confirmation alert
            Alert confirmationAlert = new Alert(AlertType.INFORMATION);
            confirmationAlert.setTitle("File Deleted");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("The file has been successfully deleted.");
            confirmationAlert.showAndWait();
          } else {
            // show error alert if the file could not be deleted
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("Delete Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("An error occurred while deleting the file.");
            errorAlert.showAndWait();
          }
        }
      } else {
        // show error alert if the selected item is not a file
        Alert errorAlert = new Alert(AlertType.ERROR);
        errorAlert.setTitle("Invalid Selection");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText("Please select a file to delete.");
        errorAlert.showAndWait();
      }
    } else {
      // show error alert if no item is selected
      Alert errorAlert = new Alert(AlertType.ERROR);
      errorAlert.setTitle("No Selection");
      errorAlert.setHeaderText(null);
      errorAlert.setContentText("Please select a file to delete.");
      errorAlert.showAndWait();
    }
  }

  // Function to delete the currently selected directory
  public void deleteSelectedDirectory() {
    TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      File selectedDirectory = new File(storageDirectory, getFilePath(selectedItem));
      if (selectedDirectory.isDirectory()) {
        // confirm deletion with the user
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Directory");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete the directory: " + selectedDirectory.getName() + "?");

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
          try {
            deleteDirectoryRecursively(selectedDirectory);
            // remove the directory from the TreeView
            TreeItem<String> parent = selectedItem.getParent();
            if (parent != null) {
              parent.getChildren().remove(selectedItem);
            } else {
              treeView.setRoot(null);
            }

            // clear the editor if the deleted directory contained the currently opened file
            if (currentFile != null && currentFile.getParentFile().equals(selectedDirectory)) {
              clearEditor();
            }

            // show confirmation alert
            Alert confirmationAlert = new Alert(AlertType.INFORMATION);
            confirmationAlert.setTitle("Directory Deleted");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("The directory has been successfully deleted.");
            confirmationAlert.showAndWait();
          } catch (IOException e) {
            // show error alert if the directory could not be deleted
            Alert errorAlert = new Alert(AlertType.ERROR);
            errorAlert.setTitle("Delete Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("An error occurred while deleting the directory.");
            errorAlert.showAndWait();
          }
        }
      } else {
          // show error alert if the selected item is not a directory
          Alert errorAlert = new Alert(AlertType.ERROR);
          errorAlert.setTitle("Invalid Selection");
          errorAlert.setHeaderText(null);
          errorAlert.setContentText("Please select a directory to delete.");
          errorAlert.showAndWait();
      }
    } else {
      // show error alert if no item is selected
      Alert errorAlert = new Alert(AlertType.ERROR);
      errorAlert.setTitle("No Selection");
      errorAlert.setHeaderText(null);
      errorAlert.setContentText("Please select a directory to delete.");
      errorAlert.showAndWait();
    }
  }

  // Function to create a new directory in the selected folder
  public void createNewDirectory() {
    TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      File selectedDirectory;
      if(selectedItem.getParent() == null) {
        selectedDirectory = storageDirectory;
      } else {
        selectedDirectory = new File(storageDirectory, getFilePath(selectedItem));
      }
      
      if (selectedDirectory.isDirectory()) {
        boolean validName = false;
        while (!validName) { // run this till user enters a valid name or closes the dialog
          // create a TextInputDialog to prompt the user for the directory name
          TextInputDialog dialog = new TextInputDialog("New Directory");
          dialog.setTitle("Create New Directory");
          dialog.setHeaderText("Enter the name of the new directory:");
          dialog.setContentText("Directory name:");

          // show the dialog and wait for the user to provide a directory name
          Optional<String> result = dialog.showAndWait();
          if (result.isPresent()) {
            String directoryName = result.get();
            File newDirectory = new File(selectedDirectory, directoryName);
            if (newDirectory.exists()) {
              // display an error alert if the directory already exists
              Alert alert = new Alert(AlertType.ERROR);
              alert.setTitle("Directory Creation Error");
              alert.setHeaderText(null);
              alert.setContentText("A directory with the same name already exists. Please enter a different name.");
              alert.showAndWait();
            } else {
                if (newDirectory.mkdir()) {
                  // add the new directory to the TreeView
                  TreeItem<String> newDirectoryItem = new TreeItem<>(newDirectory.getName());
                  selectedItem.getChildren().add(newDirectoryItem);
                  selectedItem.setExpanded(true);

                  // show confirmation alert
                  Alert confirmationAlert = new Alert(AlertType.INFORMATION);
                  confirmationAlert.setTitle("Directory Created");
                  confirmationAlert.setHeaderText(null);
                  confirmationAlert.setContentText("The directory has been successfully created.");
                  confirmationAlert.showAndWait();
                  validName = true; // exit the loop
                } else {
                  // display an error alert if the directory could not be created
                  Alert alert = new Alert(AlertType.ERROR);
                  alert.setTitle("Directory Creation Error");
                  alert.setHeaderText(null);
                  alert.setContentText("The directory could not be created.");
                  alert.showAndWait();
                }
            }
          } else {
            // user cancelled the dialog
            break;
          }
        }
      } else {
          // display an error alert if the selected item is not a directory
          Alert alert = new Alert(AlertType.ERROR);
          alert.setTitle("Invalid Selection");
          alert.setHeaderText(null);
          alert.setContentText("Please select a directory to create a new directory.");
          alert.showAndWait();
      }
    } else {
      // display an error alert if no item is selected
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No Selection");
      alert.setHeaderText(null);
      alert.setContentText("Please select a directory to create a new directory.");
      alert.showAndWait();
    }
  }


  //        ----------------------------- HELPER FUNCTIONS -----------------------------

  //                                        File Methods

  // Method to load the content of the selected file into the HTMLEditor
  private void loadFileContent(File file) {
    // check if the file has a supported extension
    String fileName = file.getName().toLowerCase();
    if (fileName.endsWith(".html") || fileName.endsWith(".htm") || fileName.endsWith(".txt")) {
      try {
          String content = new String(Files.readAllBytes(file.toPath()));
          htmlEditor.setHtmlText(content);
          htmlEditor.setVisible(true);
          currentFile = file; // Store the reference to the currently opened file
      } catch (IOException e) {
          e.printStackTrace();
      }
    } else {
      // Display an alert to the user for unsupported file type
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Unsupported File Type");
      alert.setHeaderText(null);
      alert.setContentText("The selected file type is unsupported: " + fileName);
      
      // Set the icon for the alert window
      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      stage.getIcons().add(new Image("images/icon.png"));

      alert.showAndWait();
    }
  }

  // Method to get the file path from the TreeItem
  private String getFilePath(TreeItem<String> item) {
    StringBuilder path = new StringBuilder(item.getValue());
    TreeItem<String> parent = item.getParent();
    // loop stops before reaching the root of the tree, which typically doesn't contribute to the path
    while (parent != null && parent.getParent() != null) {
        path.insert(0, parent.getValue() + File.separator);
        parent = parent.getParent();
    }
    return path.toString();
  }

  // turn on autosave feature
  private void enableAutosave() {
    htmlEditor.setOnKeyReleased(_ -> {
      if (currentFile != null) {
        try {
          String content = htmlEditor.getHtmlText();
          Files.write(currentFile.toPath(), content.getBytes());
        } catch (IOException e) {
          e.printStackTrace();
          // display an error alert to the user
          Alert alert = new Alert(AlertType.ERROR);
          alert.setTitle("Save Error");
          alert.setHeaderText(null);
          alert.setContentText("An error occurred with auto-saving the file.");
          alert.showAndWait();
        }
      } else {
        // display an error alert to the user if no file is currently opened
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("No File Opened");
        alert.setHeaderText(null);
        alert.setContentText("No file is currently opened to auto-save.");
        alert.showAndWait();
      }
    });
  }

  // turn off autosave feature
  private void disableAutosave() {
    htmlEditor.setOnKeyReleased(null);
  }

  //                                       Directory Methods

  // Allow user to select a directory of their choice
  private File selectDirectory() {
    File storedDir = getStoredDirectory(); // gets the previously stored directory

    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Choose Storage Directory");

    // address of user's home directory
    String userHome = System.getProperty("user.home");

    /* handling edge cases where:
        - user has not chosen a directory before
        - chooses directory but deletes it
        - chooses a file rather than directory 
      */
    // directory which user will be presented to choose their directory from
    File initialDir = (storedDir != null && storedDir.exists() && storedDir.isDirectory())
                        ? storedDir : new File(userHome);
                        
    directoryChooser.setInitialDirectory(initialDir);

    return directoryChooser.showDialog(null);
  }

  // Method to delete a directory and its contents recursively
  private void deleteDirectoryRecursively(File directory) throws IOException {
    if (directory.isDirectory()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          deleteDirectoryRecursively(file);
        }
      }
    }
    if (!directory.delete()) {
      throw new IOException("Failed to delete " + directory);
    }
  }


  //                                    Stored Directory Methods                                       

  // Retrieves directory of user's choice
  private File getStorageDirectory() {
    File storedDir = getStoredDirectory();
    return storedDir;
  }


  // Retrieves the stored directory path from user's local system
  private File getStoredDirectory() {
    Preferences prefs = Preferences.userNodeForPackage(Scene1Controller.class);
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

  // Store chosen directory into local system of user
  private void storeDirectory(File directory) {
    Preferences prefs = Preferences.userNodeForPackage(Scene1Controller.class);
    prefs.put(PREF_KEY_DIRECTORY, directory.getAbsolutePath());
  }

  //                                       TreeView Methods

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

  //                                        Utility Methods

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

    // update the word count label
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
  
  // Method to clear the HTML editor and reset currentFile
  private void clearEditor() {
    htmlEditor.setHtmlText("");
    htmlEditor.setVisible(false);
    currentFile = null;
  }


}