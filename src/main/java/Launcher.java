import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;

public class Launcher extends Application {
    TableView tview;
    ObservableList<Entry> entries;

    @Override
    public void start(Stage primaryStage) {
        initPrimaryStage(primaryStage);
        loadBills();
        popTView();
        primaryStage.show();
    }

    //Need this if we want to pass in command-line args
    //Also, makes for a clear explicit launch point
    public static void main(String[] args) {
        Application.launch(args);
    }

    private void initPrimaryStage(Stage primaryStage) {
        HBox hbox = topHBox(), hbox2 = bottomHBox();
        tview = tView();

        BorderPane border = new BorderPane();
        border.setTop(hbox);
        border.setCenter(tview);
        border.setBottom(hbox2);

        primaryStage.setScene(new Scene(border));
        primaryStage.setTitle("Bill Manager v4.0");
        primaryStage.setMinHeight(500);
        primaryStage.setMinWidth(800);
    }

    private HBox topHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        hbox.setAlignment(Pos.BASELINE_CENTER);

        Button buttonAdd = new Button("New Entry");
        buttonAdd.setPrefSize(100, 20);
        buttonAdd.setOnAction(event -> newEntry());

        Button buttonSearch = new Button("Search");
        buttonSearch.setOnAction(event -> search());
        buttonSearch.setPrefSize(100, 20);

        Pane spacer = new Pane();
        spacer.setMinSize(20,1);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hbox.getChildren().addAll(buttonAdd, spacer, buttonSearch);
        return hbox;
    }

    private HBox bottomHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        Button buttonDetails = new Button("View Details");
        buttonDetails.setPrefSize(100, 20);
        buttonDetails.setOnAction(event -> viewEntry());

        Button buttonPrev = new Button("<<");
        buttonPrev.setPrefSize(35,20);
        buttonPrev.setOnAction(event -> pageLeft());

        Button buttonNext = new Button(">>");
        buttonNext.setPrefSize(35,20);
        buttonNext.setOnAction(event -> pageRight());

        Button buttonStats = new Button("Statistics");
        buttonStats.setPrefSize(100, 20);
        buttonStats.setOnAction(event -> stats());

        Pane spacer = new Pane();
        spacer.setMinSize(10,1);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Pane spacer2 = new Pane();
        spacer.setMinSize(10,1);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        hbox.getChildren().addAll(buttonDetails, spacer, buttonPrev,
                buttonNext, spacer2, buttonStats);
        return hbox;
    }

    private TableView tView() {
        TableView tView = new TableView();
        TableColumn<Entry, Integer> column1 = new TableColumn<>("ID");
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));
        column1.setVisible(false);

        TableColumn<Entry, String> column2 = new TableColumn<>("Name");
        column2.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Entry, Date> column3 = new TableColumn<>("Date");
        column3.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Entry, Float> column4 = new TableColumn<>("Amount");
        column4.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Entry, Integer> column5 = new TableColumn<>("Status");
        column5.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Entry, String> column6 = new TableColumn<>("Notes");
        column6.setCellValueFactory(new PropertyValueFactory<>("notes"));

        tView.getColumns().addAll(column1, column2, column3, column4, column5, column6);
        return tView;
    }

    private void loadBills() {
        try {
            entries = FXCollections.observableArrayList();
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("select * from entry " +
                    "join bill on bill.name=entry.name " +
                    "where date >= DATE_SUB(NOW(), INTERVAL 90 DAY) " +
                    "order by date desc");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                Date date = rs.getDate(3);
                float amount = rs.getFloat(4);
                int status = rs.getInt(5);
                String notes = rs.getString(6);
                entries.add(new Entry(id, name, date, amount, status, notes));

            }
        } catch (SQLException t) { t.printStackTrace();  }
    }

    private void popTView() {
        //Clear everything
        tview.setItems(entries);

    }

    private void newEntry() {
        System.out.println("New entry");
    }

    private void search() {
        System.out.println("Search");
    }

    private void viewEntry() {
        try {
            Entry entry = (Entry) tview.getSelectionModel().getSelectedItem();
            System.out.println("Entry details "+entry.id);
        }catch (NullPointerException e) { }
    }

    private void stats() {
        System.out.println("Stats");
    }

    private void pageLeft() {
        System.out.println("Page left");
    }

    private void pageRight() {
        System.out.println("Page right");
    }

    private static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/bills";
        String username = "root";
        String password = ""; //TODO DEL PW
        return DriverManager.getConnection(url, username, password);
    }
}
