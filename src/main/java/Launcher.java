import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) {
        HBox hbox = topHBox(), hbox2 = bottomHBox();
        TableView tview = tView();

        BorderPane border = new BorderPane();
        border.setTop(hbox);
        border.setCenter(tview);
        border.setBottom(hbox2);

        primaryStage.setScene(new Scene(border));
        primaryStage.setTitle("Bill Manager v4.0");
        primaryStage.setMinHeight(500);
        primaryStage.setMinWidth(800);
        primaryStage.show();
    }

    //Need this if we want to pass in command-line args
    //Also, makes for a clear explicit launch point
    public static void main(String[] args) {
        Application.launch(args);
    }

    public HBox topHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        hbox.setAlignment(Pos.BASELINE_CENTER);

        Button buttonAdd = new Button("New Entry");
        Button buttonDel = new Button("Search");

        buttonAdd.setPrefSize(100, 20);
        buttonDel.setPrefSize(100, 20);

        Pane spacer = new Pane();
        spacer.setMinSize(20,1);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hbox.getChildren().addAll(buttonAdd, spacer, buttonDel);
        return hbox;
    }

    public HBox bottomHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        Button buttonCurrent = new Button("View Details");
        Button buttonPrev = new Button("<<");
        Button buttonNext = new Button(">>");
        Button buttonStats = new Button("Statistics");

        buttonCurrent.setPrefSize(100, 20);
        buttonPrev.setPrefSize(35,20);
        buttonNext.setPrefSize(35,20);
        buttonStats.setPrefSize(100, 20);

        Pane spacer = new Pane();
        spacer.setMinSize(10,1);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Pane spacer2 = new Pane();
        spacer.setMinSize(10,1);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        hbox.getChildren().addAll(buttonCurrent, spacer, buttonPrev,
                buttonNext, spacer2, buttonStats);
        return hbox;
    }

    public TableView tView() {
        TableView tView = new TableView();
        TableColumn<String, String> column1 = new TableColumn<>("Name");
        TableColumn<String, String> column2 = new TableColumn<>("Date");
        TableColumn<String, String> column3 = new TableColumn<>("Amount");
        TableColumn<String, String> column4 = new TableColumn<>("Status");
        TableColumn<String, String> column5 = new TableColumn<>("Notes");

        tView.getColumns().addAll(column1, column2, column3, column4, column5);

        //column1.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        //tView.getItems().add(new Person("John", "Doe"));



        return tView;
    }
}
