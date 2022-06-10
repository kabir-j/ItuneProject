package cs1302.gallery;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.TilePane;
import javafx.scene.control.ProgressBar;

import javafx.scene.control.ChoiceBox;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;




import javafx.animation.KeyFrame;

import java.time.LocalTime;
import javafx.util.Duration;
import javafx.animation.Timeline;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ComboBox;

/**
 * Represents an iTunes GalleryApp.
 */
public class GalleryApp extends Application {
    //main vbox that houses all objects
    private VBox main = new VBox();
    private boolean playBoolean = false;

    //last row in window
    HBox hStatus = new HBox();
    ProgressBar progressBar = new ProgressBar();
    Text info = new Text("Images Provided by iTunes Search API.");
    //Toolbar, first row
    Button pause = new Button("Play");
    Button update = new Button("Get Images");
    Text search = new Text("Search:");
    TextField textField = new TextField("Kabir");
    ChoiceBox<String> choice = new ChoiceBox<>();
    ToolBar toolBar = new ToolBar(pause,search,textField, choice, update);
    //second row
    Text instructions = new Text("Type in a term, select a media type, then click the button.");
    //swapping
    Timeline timeline;
    Random rand = new Random();
    ImageView[] otherImgViews;
    int width;
    //imageview
    TilePane tilePane = new TilePane();
    ImageView[] imgViews = new ImageView[20];   //alert
    Alert alert = new Alert(AlertType.ERROR);
    boolean isFirstRun = true;

    /**
     * Sets the behavior and charectaristics of the different features.
     *{@inheritdoc}
     */
    @Override
    public void init() {
        //editing size of bar
        progressBar.setPrefSize(260,20);
        //creating options for dropwdown
        choice.getItems().add("movie");
        choice.getItems().add("podcast");
        choice.getItems().add("music");
        choice.getItems().add("musicVideo");
        choice.getItems().add("audioBook");
        choice.getItems().add("shortFilm");
        choice.getItems().add("tvShow");
        choice.getItems().add("software");
        choice.getItems().add("ebook");
        choice.getItems().add("all");
        choice.setValue("music");
        main.getChildren().add(toolBar);
        main.getChildren().add(instructions);
        //calls to set on action using created methods for event handles
        update.setOnAction(this::updateImages);
        pause.setOnAction(this::play);
        //center view using default image
        tilePane.setPrefColumns(5);
        for ( int i = 0 ; i < imgViews.length ; i++ ) {
            imgViews[i] = new ImageView(new Image("file:resources/default.png"));
            imgViews[i].setPreserveRatio(true);
            tilePane.getChildren().add(imgViews[i]);
        }
        pause.setDisable(true);
        main.getChildren().addAll(tilePane);
        hStatus.getChildren().addAll(progressBar, info);
        main.getChildren().add(hStatus);

        //timeline
        EventHandler<ActionEvent> handler = event -> swap();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
    }


    /** Fires the update button and sets the size of the main window.
        {@inheritdoc} */
    @Override
    public void start(Stage stage) {

        update.fire(); //for intitial query
        Scene scene = new Scene(main);
        stage.setMinWidth(490);
        stage.setMinHeight(480.00);
        //stage.setMaxHeight(480);
        stage.setTitle("GalleryApp!");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.sizeToScene();

        stage.show();
        pause.fire();

    } // start


    /** Sets the progress of the progress bar on the
     * java application thread.
     * @param progress the incriment to increase
     */

    private void setProgress(final double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    } // setProgress

    /**
     *Sets the new images on the javafx application thread.
     *@param img the image to add
     *@param tp the tilepane to add on to
     *@param i the index to add on to
     */
    private void setImage (final Image img, final TilePane tp, final int i) {
        Platform.runLater( () -> tp.getChildren().set(i, new ImageView(img)) );
    }

    /**
     * Creates an alert box when there are less then 21
     *items in a query.
     */
    private void alertUnder21() {

        Thread alertThread = new Thread ( () -> {
            alert.setTitle("Error");
            alert.setHeaderText("Error");// line 3
            String message = ("There were less then 21 unique images found.");
            alert.setContentText(message);
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(480, 320);
            instructions.setText("Last attempt to get images failed...");
            Platform.runLater(() -> alert.show()); //showing thr alert
        });
        alertThread.setDaemon(true); //run thread
        alertThread.start();

    }

    /**
     *Creates an alert box when faced with IOException.
     */
    private void urlError() {
        Thread alertThread = new Thread ( () -> {
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            String message = ("Exception:java.io.IOException:"
                + "There was an error processing your query");
            alert.setContentText(message);
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(480, 320);
            instructions.setText("Last attempt to get images failed...");
            Platform.runLater(() -> alert.show()); //showing thr alert
        });
        alertThread.setDaemon(true); //run thread
        alertThread.start();

    }

    /**
     *Creates an alert box when faced with an Interrupted Exception.
     */
    private void downloadErr() {

        Thread alertThread = new Thread ( () -> {
            alert.setTitle("Error");
            alert.setHeaderText("Error");// line 3
            String message = ("Exceptionjava.lang.InterruptedException:"
                + "There was an error with your query.");
            alert.setContentText(message);
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(480, 320);
            instructions.setText("Last attempt to get images failed...");
            Platform.runLater(() -> alert.show()); //showing thr alert
        });
        alertThread.setDaemon(true); //run thread
        alertThread.start();

    }

    /**
     * Represents a response from the iTunes Search API. This is used by Gson to
     * create an object from the JSON response body.
     *
     * <pre>
     * {
     *   "resultCount": 3,
     *   "results": [
     *     ItunesResult object,
     *     ItunesResult object,
     *     ItunesResult object
     *   ]
     * }
     * </pre>
     */
    private static class ItunesResponse {
        int resultCount;         // package private visibility is intentional
        ItunesResult[] results;  // if you make these, private, then add getters
    } // ItunesResponse

    /**
     * Represents a result in a response from the iTunes Search API. This is
     * used by Gson to create an object from the JSON response body.
     *
     * <pre>
     * {
     *   "wrapperType": "track",
     *   "kind": "song",
     *   ...,
     *   "artworkUrl100": "https://.../source/100x100bb.jpg",
     *   ...
     * }
     * </pre>
     */
    private static class ItunesResult {
        String wrapperType;   // package private visibility is intentional
        String kind;          // if you make these, private, then add getters
        String artworkUrl100; // we omit variables for data we're not interested in
    } // ItunesResult

    private static HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns an HttpClient

    private static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private static final String ITUNES_API = "https://itunes.apple.com/search";

    /** Updates the images in the TilePane.
     * @param e the action even to be used
     */

    public void updateImages(ActionEvent e) {
        boolean updateBoolean = playBoolean;
        if (playBoolean) {
            pause.fire();
        } //change play button
        Thread thread = new Thread( () -> {
            update.setDisable(true); //disable button
            try { //creating array pof images
                this.queryProcess();
            } catch ( Exception updateException) {
                System.out.println(updateException.getMessage());
            }
            Platform.runLater( () -> { //update play button bool
                if (updateBoolean) {
                    pause.fire();
                }
            });
        });
        thread.setDaemon(true);
        thread.start(); //start thread
    }

    /**
     *Creates url query to send to api, and then creates the arrays to store.
     */
    public void queryProcess() {
        try {
            String term = URLEncoder.encode(textField.getText(), StandardCharsets.UTF_8);
            String media = URLEncoder.encode((String)choice.getValue(), StandardCharsets.UTF_8);
            String limit = URLEncoder.encode("200", StandardCharsets.UTF_8);
            String query = String.format("?term=%s&media=%s&limit=%s", term, media, limit);
            String uri = ITUNES_API + query;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

            String jsonString = response.body();
            ItunesResponse itunesResponse = GSON
                .fromJson(jsonString, GalleryApp.ItunesResponse.class);
            HashSet<String> set = new HashSet<>(); //ignoring all duplicates
            for (int i = 0; i < itunesResponse.results.length; i++) {
                ItunesResult result = itunesResponse.results[i];
                set.add(result.artworkUrl100);
            } // for
            ArrayList<String> urlArray = new ArrayList<>(set); //unique array
            if (isFirstRun == true) {
                isFirstRun = false;
            } else {
                if (urlArray.size() < 21) {
                    alertUnder21();
                } else {
                    instructions.setText("Getting Images...");
                    pause.setDisable(true);
                    setProgress(0); //progress bar 0
                    int index = 0;
                    otherImgViews = new ImageView[urlArray.size() - 20];
                    for (int i = 0 ; i < urlArray.size() ; i++) {
                        Image albumImage = new Image(urlArray.get(i), 100, 100, false, false);
                        if ( i < 20 ) { //adding to tilepane
                            setProgress(1.0 * i / 20);
                            setImage(albumImage, tilePane, i);
                            imgViews[i] = new ImageView(albumImage);
                        } else {
                            otherImgViews[index] = new ImageView(albumImage);
                            index++;
                        } //creating array of remaining images
                    }
                    pause.setDisable(false);
                    instructions.setText(uri);
                    setProgress(1);
                }
            }
            update.setDisable(false);
        } catch (IOException ie) {
            urlError();
        } catch (InterruptedException intE) {
            downloadErr();
        }

    }

    /** Performs a swap between a used and unused image. */
    public void swap() {
        int currentRand = rand.nextInt(imgViews.length - 1); //random indecies
        int remainingRand = rand.nextInt(otherImgViews.length - 1);
        ImageView item = imgViews[currentRand];
        ImageView otherItem = otherImgViews[remainingRand];
        Platform.runLater( () -> tilePane.getChildren().set(currentRand, otherItem) ); //update
        imgViews[currentRand] = otherItem; //swap
        otherImgViews[remainingRand] = item;
    }

    /** Uses a boolean to switch between the play/button actions.
     * @param playEvent the action event for the play/pause functionality
     */
    public void play(ActionEvent playEvent) {
        if (playBoolean) { //button switcing
            playBoolean = false;
            pause.setText("Play");
        } else {
            playBoolean = true;
            pause.setText("Pause");
        }
        try { //action switching
            if (playBoolean) {
                Thread playThread = new Thread ( () -> {
                    timeline.play();
                });
                playThread.setDaemon(true);
                playThread.start();
            } else {
                timeline.pause();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
} // GalleryApp
