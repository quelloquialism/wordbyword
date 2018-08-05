package com.atsymboldot.wordbyword;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.atsymboldot.wordbyword.source.ScannerTextSource;
import com.atsymboldot.wordbyword.source.TextSource;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A simple JavaFX application for reading text files by displaying each word, one by one, for a
 * small amount of time. Intended to improve reading speed and consistent focus.
 * 
 * <p>Suitable reading files can be excerpted from Project Gutenberg titles at
 * https://www.gutenberg.org/.
 */
public final class WordByWord extends Application {
  private static final double WINDOW_WIDTH_PX = 190;
  private static final double WINDOW_HEIGHT_PX = 150;
  private static final double MINIMUM_SPEED_WPS = 1;
  private static final double MAXIMUM_SPEED_WPS = 15;
  private static final double DEFAULT_SPEED_WPS = 2.5;
  private static final String INVALID_FILE_ERROR = "The selected file could not be read. Please try again.";
  
  private Text readingText = new Text();
  private File selectedFile;
  private Timeline readingPlayback = new Timeline();
  // Current playback timing.
  private double millisPerWord = wpsToMillisPerWord(DEFAULT_SPEED_WPS);
  
  public static void main(String[] args) throws FileNotFoundException {
    launch();
  }
  
  @Override
  public void start(Stage stage) throws IOException {
    initializeUi(stage);
  }
  
  /**
   * Builds the JavaFX GUI for reading, including file selection, a series of playback controls,
   * and the output text itself.
   * 
   * @param stage The primary JavaFX container holding the application GUI.
   */
  private void initializeUi(Stage stage) {
    stage.setTitle("Word-by-word");
    GridPane grid = new GridPane();
    grid.setHgap(5);
    grid.setVgap(5);
    
    Button selectFileButton = new Button("Select file...");
    selectFileButton.setOnAction((event) -> chooseFile(stage));
    readingText.setText("Select a file.");
    Slider speedSlider =new Slider(MINIMUM_SPEED_WPS, MAXIMUM_SPEED_WPS, DEFAULT_SPEED_WPS);
    speedSlider.addEventHandler(EventType.ROOT, (event) -> updateSpeed(speedSlider.getValue()));
    Label sliderLabel = new Label("Playback speed");
    Button stopButton = new Button("Stop");
    stopButton.setOnAction((event) -> readingPlayback.stop());
    Button pauseButton = new Button("Pause");
    pauseButton.setOnAction((event) -> readingPlayback.pause());
    Button startButton = new Button("Start reading");
    startButton.setOnAction((event) -> readingPlayback.play());
    
    // Constraints order is: column, row, colspan, rowspan
    GridPane.setConstraints(selectFileButton, 0, 0, 3, 1);
    GridPane.setConstraints(
        readingText, 0, 1, 3, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
    GridPane.setConstraints(speedSlider, 0, 2, 3, 1, HPos.CENTER, VPos.CENTER);
    GridPane.setConstraints(sliderLabel, 0, 3, 3, 1, HPos.CENTER, VPos.TOP);
    GridPane.setConstraints(stopButton, 0, 4);
    GridPane.setConstraints(pauseButton, 1, 4);
    GridPane.setConstraints(startButton, 2, 4);
    
    grid.getChildren().addAll(
        selectFileButton,
        readingText,
        speedSlider,
        sliderLabel,
        startButton,
        pauseButton,
        stopButton);
    stage.setScene(new Scene(grid, WINDOW_WIDTH_PX, WINDOW_HEIGHT_PX));
    stage.show();
  }
  
  /**
   * Updates the reading playback speed when the user triggers the speed slider. If a file has
   * already been selected, this requires regenerating the playback timeline, which can be costly
   * if the source text is long.
   * 
   * @param wordsPerSecond
   */
  private void updateSpeed(double wordsPerSecond) {
    double millisPerWord = wpsToMillisPerWord(wordsPerSecond);
    if (this.millisPerWord != millisPerWord) {
      this.millisPerWord = millisPerWord;
      if (selectedFile != null) {
        try {
          initializePlayback();
        } catch (IOException e) {
          Alert fileFailure = new Alert(AlertType.ERROR);
          fileFailure.setContentText(INVALID_FILE_ERROR);
          fileFailure.show();
        }
      }
    }
  }
  
  /**
   * Spawns a local file browser to allow the user to choose a text file to read. After a valid
   * file is selected, it is immediately parsed and mapped to an event timeline ready to be
   * displayed in the main UI.
   * 
   * @param stage The primary JavaFX container holding the application GUI.
   */
  private void chooseFile(Stage stage) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select source text file");
    selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      try {
        initializePlayback();
      } catch (IOException e) {
        Alert fileFailure = new Alert(AlertType.ERROR);
        fileFailure.setContentText(INVALID_FILE_ERROR);
        fileFailure.show();
      }
    }
  }
  
  /**
   * Builds a timeline of events for each word/token parsed from the given input file.
   * 
   * <p>Prior to calling this function, the user should have selected a file to read.
   * 
   * @param file The input file to parse.
   * @throws IOException if the file cannot be read or does not exist.
   * @throws NullPointerException if no file has been selected.
   */
  private void initializePlayback() throws IOException {
    readingPlayback.stop();
    readingPlayback.getKeyFrames().clear();
    TextSource source = new ScannerTextSource(selectedFile);
    double currentTime = 0;
    for (int i = 3; i > 0; i--) {
      updateTextAtTime(currentTime, i + "...");
      currentTime += millisPerWord;
    }
    while (source.hasNextWord()) {
      updateTextAtTime(currentTime, source.getNextWord());
      currentTime += millisPerWord;
    }
    updateTextAtTime(currentTime, "Reading complete.");
    readingText.setText("Ready.");
  }
  
  /**
   * Adds a single text update command to the reading playback timeline at a specified timepoint.
   * 
   * @param millisTime The timestamp, in milliseconds, of the text update event.
   * @param text The new text value to write to the timeline.
   */
  private void updateTextAtTime(double millisTime, String text) {
    Duration timePoint = new Duration(millisTime);
    KeyValue textUpdate = new KeyValue(readingText.textProperty(), text);
    readingPlayback.getKeyFrames().add(new KeyFrame(timePoint, textUpdate));
  }
  
  /**
   * Conversion utility for converting playback speed inputs (in words per second) to corresponding
   * time increments (in milliseconds per word).
   * 
   * @param wordsPerSecond The playback speed pace in words per second.
   * @return The playback speed pace in milliseconds per word.
   */
  private static double wpsToMillisPerWord(double wordsPerSecond) {
    return 1000. / wordsPerSecond;
  }
}
