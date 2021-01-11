package sample;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;

import static sample.ProducerConsumer.*;



public class Controller implements Initializable {

    // search button
    @FXML
    Button searchBtn;
    // directory name field
    @FXML
    TextField directoryName;
    // file name field
    @FXML
    TextField inputFileName;
    // output count label
    @FXML
    Label countNum;
    // output file list
    @FXML
    TextArea outputList;


    // flag for work done
    volatile static boolean isDone = false;
    volatile static boolean isOutputDone = false;
    // Root file path, input from text field
    volatile static String startPath = "";

    // input from text field
    volatile static String fileName = "";

   // volatile static ExecutorService service = Executors.newFixedThreadPool(8);
    // fileCount is binding to count
    static ObservableList<String> fileCount = FXCollections.observableArrayList("");
    // filesOutput is binding to the search results
    static ObservableList<String> filesOutput = FXCollections.observableArrayList("");

    // Engine thread
    Runnable task = new OutputEngine();
    Thread t = new Thread(task);

    /**
     * search handler
     * @param evt
     */
    public void searchHandler(ActionEvent evt) {

        isDone = false;

        isOutputDone = false;

        synchronized(syncalFileList) {
            syncalFileList.clear();
        }


        // get directory name
        startPath = directoryName.getText();
      //    get file name or type
        fileName = inputFileName.getText();
          // start the search
        startIndexing();

    }

    /**
     * initialize binding and start outputEngine thread
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // binding file count
        countNum.textProperty().bind(Bindings.stringValueAt(fileCount, 0));
        // binding file list
        outputList.textProperty().bind(Bindings.stringValueAt(filesOutput,0));
        // make textarea scrollable
        outputList.setEditable(false);
        t.start();

    }



}


