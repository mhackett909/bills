import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Date;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

public class BillView {
    private TableView tview;
    private Stage primaryStage, searchStage;
    private VBox topSearchBox, leftSearchBox, rightSearchBox;
    private ComboBox searchCombo;
    private ObservableList<Entry> entries;
    private ArrayList<Bill> bills;

    //Primary Stage
    protected void initPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
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

    //Search Stage
    private void initSearchStage(Stage searchStage) {
        searchStage.initModality(Modality.WINDOW_MODAL);
        searchStage.initOwner(primaryStage);

        topSearchBox = topSearchBox();
        leftSearchBox = leftSearchBox();
        rightSearchBox = rightSearchBox();
        HBox hbox = bottomSearchBox();

        BorderPane border = new BorderPane();
        border.setTop(topSearchBox);
        border.setLeft(leftSearchBox);
        border.setRight(rightSearchBox);
        border.setBottom(hbox);

        searchStage.setScene(new Scene(border));
        searchStage.setTitle("Search");
        searchStage.setResizable(false);
    }

    private VBox topSearchBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15, 12, 15, 12));
        vbox.setSpacing(10);
        vbox.setStyle("-fx-background-color: #336699;");
        vbox.setAlignment(Pos.BASELINE_CENTER);

        searchCombo = new ComboBox();
        searchCombo.setPrefSize(100,25);

        CheckBox chk = new CheckBox("Include Inactive");
        chk.setTextFill(Color.WHITE);
        chk.setOnAction(event -> popSearchCombo(chk.isSelected()?true:false));

        vbox.getChildren().addAll(searchCombo, chk);
        return vbox;
    }

    private HBox bottomSearchBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        hbox.setAlignment(Pos.BASELINE_CENTER);

        Button submit = new Button("Search");
        submit.setPrefSize(100, 20);
        submit.setOnAction(event -> searchEntry());

        hbox.getChildren().addAll(submit);
        return hbox;
    }

    private VBox leftSearchBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15, 12, 15, 12));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.BASELINE_CENTER);

        DatePicker date = new DatePicker();
        date.setPrefSize(100, 20);
        date.setValue(LocalDate.now().minusDays(90));
        date.setDisable(true);

        DatePicker date2 = new DatePicker();
        date2.setValue(LocalDate.now());
        date2.setPrefSize(100, 20);
        date2.setDisable(true);

        ToggleGroup radioGroup = new ToggleGroup();

        RadioButton all = new RadioButton("All Time");
        all.setToggleGroup(radioGroup);
        all.setPrefSize(100, 20);
        all.setSelected(true);
        all.setOnAction(event -> toggleDatePickers(date, date2));

        RadioButton byDate = new RadioButton("Date Range");
        byDate.setToggleGroup(radioGroup);
        byDate.setPrefSize(100, 20);
        byDate.setOnAction(event -> toggleDatePickers(date, date2));

        Label text = new Label(" - ");
        text.setStyle("-fx-font-size: 15;");

        HBox hbox = new HBox(), hbox2 = new HBox();
        hbox.getChildren().addAll(all, byDate);
        hbox2.getChildren().addAll(date, text, date2);

        vbox.getChildren().addAll(hbox, hbox2);
        return vbox;
    }

    private VBox rightSearchBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15, 12, 15, 12));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.BASELINE_CENTER);
        vbox.setStyle("-fx-background-color: lightgray;");

        TextField min = new TextField();
        min.setPrefSize(75, 20);
        min.setPromptText("Min");
        min.setDisable(true);

        TextField max = new TextField();
        max.setPrefSize(75, 20);
        max.setPromptText("Max");
        max.setDisable(true);

        CheckBox chk = new CheckBox("Paid");
        chk.setSelected(true);

        CheckBox chk2 = new CheckBox("Due");
        chk2.setSelected(true);

        CheckBox chk3 = new CheckBox("Overpaid");
        chk3.setSelected(true);

        CheckBox chk4 = new CheckBox("By amount");
        chk4.setOnAction(event -> toggleAmountFields(min, max));

        Label text = new Label(" - ");
        text.setStyle("-fx-font-size: 15;");

        Pane spacer = new Pane();
        spacer.setMinSize(10,1);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Pane spacer2 = new Pane();
        spacer2.setMinSize(10,1);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox hbox = new HBox(), hbox2 = new HBox(), hbox3 = new HBox();
        hbox.getChildren().addAll(chk, spacer, chk2, spacer2, chk3);
        hbox2.getChildren().addAll(chk4);
        hbox3.getChildren().addAll(min, text, max);

        vbox.getChildren().addAll(hbox, hbox2, hbox3);
        return vbox;
    }

    //Helper methods
    private void toggleDatePickers(DatePicker date, DatePicker date2) {
        date.setDisable(date.isDisabled()?false:true);
        date2.setDisable(date2.isDisabled()?false:true);
    }

    private void toggleAmountFields(TextField field, TextField field2) {
        field.setDisable(field.isDisabled()?false:true);
        field2.setDisable(field2.isDisabled()?false:true);
    }

    private void search() {
        searchStage = new Stage();
        initSearchStage(searchStage);
        popSearchCombo(false);
        searchStage.showAndWait();
    }

    private void searchEntry() {
        String bill = ((ComboBox) topSearchBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
        StringBuilder statement = new StringBuilder();
        statement.append("select * from entry join bill on bill.name=entry.name where");

        HBox hbox = (HBox) rightSearchBox.getChildren().get(0);
        boolean getPaid = ((CheckBox) hbox.getChildren().get(0)).isSelected();
        boolean getDue = ((CheckBox) hbox.getChildren().get(2)).isSelected();
        boolean getOP = ((CheckBox) hbox.getChildren().get(4)).isSelected();

        ArrayList<String> list = new ArrayList<>();
        if (getDue) list.add("0");
        if (getPaid) list.add("1");
        if (getOP) list.add("2");
        if (list.size() == 0) list.add("3"); //Dummy "invalid" value
        String fullList="";
        for (String next : list) fullList += next + ",";
        fullList = fullList.substring(0, fullList.length() - 1);
        statement.append(" entry.status in (");
        statement.append(fullList);
        statement.append(")");
        boolean allBills = bill.equals("All Bills");
        if (!allBills) {
            statement.append(" and bill.name=\'");
            statement.append(bill);
            statement.append("\'");
        }

        hbox = (HBox) rightSearchBox.getChildren().get(1);
        boolean usingRange = ((CheckBox) hbox.getChildren().get(0)).isSelected();
        float min = 0;
        float max = Float.MAX_VALUE;
        if (usingRange) {
            hbox = (HBox) rightSearchBox.getChildren().get(2);
            try {
                min = Float.parseFloat(((TextField) hbox.getChildren().get(0)).getText());
            }catch (NumberFormatException e) { }
            try {
                max = Float.parseFloat(((TextField) hbox.getChildren().get(2)).getText());
            }catch (NumberFormatException e) { }
            statement.append(" and amount between ");
            statement.append(min);
            statement.append(" and ");
            statement.append(max);
        }

        hbox = (HBox) leftSearchBox.getChildren().get(0);
        boolean usingDate = ((RadioButton) hbox.getChildren().get(1)).isSelected();
        if (usingDate) {
            hbox = (HBox) leftSearchBox.getChildren().get(1);
            LocalDate date1 = ((DatePicker) hbox.getChildren().get(0)).getValue();
            LocalDate date2 = ((DatePicker) hbox.getChildren().get(2)).getValue();
            statement.append(" and date between \'");
            statement.append(Date.valueOf(date1));
            statement.append("\' and \'");
            statement.append(Date.valueOf(date2));
            statement.append("\'");
        }
        statement.append(" order by date desc");
        searchStage.close();
        entryPop(statement.toString());
        popTView();
    }

    private void popSearchCombo(boolean all) {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Bill bill : bills) {
            if (bill.isActive() || all) items.add(bill.getName());
        }
        searchCombo.setItems(items);
        searchCombo.getSelectionModel().selectFirst();
    }

    protected void loadBills() {
        try {
            bills = new ArrayList<>();
            Connection conn = Launcher.getConnection();
            PreparedStatement ps = conn.prepareStatement("select * from bill");
            ResultSet rs = ps.executeQuery();
            bills.add(new Bill("All Bills", true));
            while(rs.next()) {
                bills.add(new Bill(rs.getString(1), rs.getBoolean(2)));
            }
        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void entryPop(String statement) {
        try {
            entries = FXCollections.observableArrayList();
            Connection conn = Launcher.getConnection();
            PreparedStatement ps = conn.prepareStatement(statement);
            //System.out.println("Exec: "+statement);
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
        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void popTView() {
        tview.refresh();
        tview.setItems(entries);
    }

    private void viewEntry() {
        try {
            Entry entry = (Entry) tview.getSelectionModel().getSelectedItem();
            System.out.println("Entry details "+entry.id);
        }catch (NullPointerException e) { }
    }

    private void newEntry() {
        System.out.println("New entry");
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
}
