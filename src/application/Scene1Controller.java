package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeView;
import javafx.scene.web.HTMLEditor;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

public class Scene1Controller {
    @FXML
    private HTMLEditor htmlEditor; // User makes there notes here

    @FXML
    private TreeView<String> treeView; // Displays names of notes
    
    @FXML
    private ScrollPane treeScrollPane; // Treeview Scroll Pane
    
    @FXML
    private BorderPane borderPane; // Contains Scene

    @FXML
    private Button toggleTreeViewButton; // toggle the TreeView

    @FXML
    private Label wordCountLabel; // Add a label in your FXML for the word count

    public void initialize() {
        // Use a key released event to update the word count from the HTMLEditor content.
        htmlEditor.setOnKeyReleased(event -> {
            updateWordCount(htmlEditor.getHtmlText());
        });

        // Set up the toggle button to show/hide the TreeView.
        toggleTreeViewButton.setOnAction(event -> {
            // Check the visibility of the TreeView.
            if (treeView.isVisible()) {
                // Hide the TreeView by removing the ScrollPane from the left side.
                treeView.setVisible(false); // Optionally, you can keep the TreeView visible but empty.
                borderPane.setLeft(null);  // Remove the ScrollPane (containing the TreeView)
                toggleTreeViewButton.setText("Show Notes List");
            } else {
                // Show the TreeView by re-adding the ScrollPane to the left side.
                borderPane.setLeft(treeScrollPane); // Add the ScrollPane back
                treeView.setVisible(true); // Make sure the tree is visible
                toggleTreeViewButton.setText("Hide Notes List");
            }
        });
    }

    /**
 * Updates the word count displayed in the wordCountLabel.
 * This method handles new lines by first replacing <br> and </p> tags with newline characters,
 * then removing all remaining HTML tags, and finally splitting the plain text to count words.
 *
 * @param htmlText the HTML content from the HTMLEditor.
 */
private void updateWordCount(String htmlText) {
  // Get HTML text from the HTMLEditor (you could also use the provided htmlText parameter)
  String text = htmlEditor.getHtmlText();

  // Replace common HTML tags that indicate a line break with an actual newline character.
  // The (?i) makes the regex case-insensitive.
  text = text.replaceAll("(?i)<br\\s*/?>", "\n");
  text = text.replaceAll("(?i)</p>", "\n");

  // Now remove all remaining HTML tags.
  String plainText = text.replaceAll("<[^>]+>", "");

  // Trim the plain text and split on any whitespace (including newlines, spaces, and tabs)
  // This will properly handle text that is split across multiple lines.
  String[] words = plainText.trim().split("\\s+");

  // Calculate the word count. If the plainText is empty after trimming, count is 0.
  int wordCount = plainText.trim().isEmpty() ? 0 : words.length;

  // Output for debugging purposes
  System.out.println("Word Count: " + wordCount);

  // Update the word count label if it exists.
  if (wordCountLabel != null) {
      if(wordCount > 1) 
        wordCountLabel.setText(wordCount + " Words");
      else
        wordCountLabel.setText("1 Word");
  } else {
      System.err.println("Reload");
  }
}

}