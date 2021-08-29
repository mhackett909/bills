import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class BillView {
    private TableView tview;
    private Stage primaryStage, searchStage, newStage, editStage;
    private Stage viewStage, paymentStage, statStage;
    private VBox topSearchBox, leftSearchBox, rightSearchBox;
    private VBox topNewBox, centerNewBox, centerEditBill;
    private HBox bottomEditBill;
    private ComboBox searchCombo, newCombo;
    private CheckBox newBillchk;
    private Launcher controller;

    public BillView(Launcher controller) {
        this.controller = controller;
    }
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
        primaryStage.setTitle("Bill Manager V4");
        primaryStage.setMinHeight(500);
        primaryStage.setMinWidth(800);
    }

    private HBox topHBox() {
        HBox hbox = genHBox();
        hbox.setStyle("-fx-background-color: #336699;");

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
        HBox hbox = genHBox();
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
        TableColumn<BillData.Entry, Integer> column1 = new TableColumn<>("ID");
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));
        column1.setVisible(false);

        TableColumn<BillData.Entry, String> column2 = new TableColumn<>("Name");
        column2.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<BillData.Entry, Date> column3 = new TableColumn<>("Date");
        column3.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<BillData.Entry, Float> column4 = new TableColumn<>("Amount");
        column4.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<BillData.Entry, Integer> column5 = new TableColumn<>("Status");
        column5.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<BillData.Entry, String> column6 = new TableColumn<>("Notes");
        column6.setCellValueFactory(new PropertyValueFactory<>("notes"));

        tView.getColumns().addAll(column1, column2, column3, column4, column5, column6);
        return tView;
    }

    //Search Stage
    private void initSearchStage() {
        searchStage = new Stage();
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
        VBox vbox = genVBox();

        vbox.setStyle("-fx-background-color: #336699;");

        searchCombo = new ComboBox();
        searchCombo.setPrefSize(100,25);

        CheckBox chk = new CheckBox("Include Inactive");
        chk.setTextFill(Color.WHITE);
        chk.setOnAction(event -> controller.popSearchCombo(chk.isSelected()));

        vbox.getChildren().addAll(searchCombo, chk);
        return vbox;
    }

    private HBox bottomSearchBox() {
        HBox hbox = genHBox();
        hbox.setStyle("-fx-background-color: #336699;");

        Button submit = new Button("Search");
        submit.setPrefSize(100, 20);
        submit.setOnAction(event -> searchEntry());

        hbox.getChildren().addAll(submit);
        return hbox;
    }

    private VBox leftSearchBox() {
        VBox vbox = genVBox();

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
        VBox vbox = genVBox();
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

    //New Entry Stage
    private void initNewStage() {
        newStage = new Stage();
        newStage.initModality(Modality.WINDOW_MODAL);
        newStage.initOwner(primaryStage);

        topNewBox = topNewBox();
        centerNewBox = centerNewBox();
        HBox bottomNewBox = bottomNewBox();

        BorderPane border = new BorderPane();
        border.setTop(topNewBox);
        border.setCenter(centerNewBox);
        border.setBottom(bottomNewBox);

        newStage.setScene(new Scene(border));
        newStage.setTitle("New Entry");
        newStage.setResizable(false);
    }

    private VBox topNewBox() {
        VBox vbox = genVBox();
        vbox.setStyle("-fx-background-color: #336699;");

        newCombo = new ComboBox();
        newCombo.setPrefSize(200, 20);

        Button newBill = new Button("New");
        newBill.setPrefSize(100, 20);
        newBill.setOnAction(event -> createBill());

        Button viewBill = new Button("View/Edit");
        viewBill.setPrefSize(100, 20);
        viewBill.setOnAction(event -> editBill());

        HBox hbox = new HBox();
        hbox.getChildren().addAll(newBill, viewBill);

        newBillchk = new CheckBox("Include Inactive");
        newBillchk.setTextFill(Color.WHITE);
        newBillchk.setOnAction(event -> controller.popNewCombo(newBillchk.isSelected()));

        vbox.getChildren().addAll(newCombo, hbox, newBillchk);
        return vbox;
    }

    private VBox centerNewBox() {
        VBox vbox = genVBox();

        DatePicker date = new DatePicker();
        date.setPrefSize(100, 20);
        date.setValue(LocalDate.now());

        TextField amount = new TextField();
        amount.setPromptText("Amount");
        amount.setPrefSize(100, 20);

        TextField notes = new TextField();
        notes.setPromptText("Notes");
        notes.setPrefSize(200, 20);

        vbox.getChildren().addAll(date, amount, notes);
        return vbox;
    }


    private HBox bottomNewBox() {
        HBox hbox = genHBox();

        Button submit = new Button("Submit");
        submit.setPrefSize(100, 20);
        submit.setOnAction(event -> submitEntry());

        hbox.getChildren().addAll(submit);
        return hbox;
    }

    //Edit Bill Stage
    private void editBill() {
        String currentBill = "";
        try {
            currentBill = ((ComboBox) topNewBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
        }catch (NullPointerException e) { }
        if (currentBill.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot View Bill");
            alert.setContentText("Oops, no bill is selected! Please select or create one");
            alert.showAndWait();
            return;
        }
        editStage = new Stage();
        editStage.initModality(Modality.WINDOW_MODAL);
        editStage.initOwner(newStage);

        HBox topEditBill = topEditBill();
        centerEditBill = centerEditBill();
        bottomEditBill = bottomEditBill();

        BorderPane border = new BorderPane();
        border.setTop(topEditBill);
        border.setCenter(centerEditBill);
        border.setBottom(bottomEditBill);

        editStage.setScene(new Scene(border));
        editStage.setTitle("View/Edit Bill");
        editStage.setResizable(false);

        editStage.showAndWait();
    }

    HBox topEditBill() {
        HBox hbox = genHBox();

        CheckBox chk = new CheckBox("Edit");
        chk.setTextFill(Color.WHITE);
        chk.setOnAction(event -> toggleBillEdit(chk.isSelected()));

        hbox.getChildren().addAll(chk);
        return hbox;
    }

    VBox centerEditBill() {
        VBox vbox = genVBox();

        String selectedName = ((ComboBox) topNewBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
        BillData.Bill currentBill = controller.getBillByName(selectedName);

        TextField input = new TextField();
        input.setPrefSize(100, 20);
        input.setText(currentBill.getName());
        input.setDisable(true);

        CheckBox chk = new CheckBox("Active");
        chk.setPrefSize(100, 20);
        chk.setSelected(currentBill.isActive());
        chk.setDisable(true);

        vbox.getChildren().addAll(input,chk);
        return vbox;

    }

    HBox bottomEditBill() {
        HBox hbox = genHBox();

        Button save = new Button("Save");
        save.setPrefSize(100, 20);
        save.setOnAction(event -> saveBill());
        save.setDisable(true);

        Button del = new Button("Delete");
        del.setPrefSize(100, 20);
        del.setOnAction(event -> delBill());
        del.setTextFill(Color.RED);
        del.setDisable(true);

        hbox.getChildren().addAll(save,del);
        return hbox;
    }

    //Edit Stage Helper Methods
    private void saveBill() {
        String newName = ((TextField) centerEditBill.getChildren().get(0)).getText().strip();
        String oldName = ((ComboBox) topNewBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
        boolean setActive = ((CheckBox) centerEditBill.getChildren().get(1)).isSelected();
        boolean oldActive = controller.getBillByName(oldName).isActive();
        if (controller.verifyName(newName)){
            try {
                controller.renameBill(oldName, newName, setActive);
                controller.loadBills();
                editStage.close();
                controller.popNewCombo(newBillchk.isSelected());
                newCombo.getSelectionModel().select(newName);
            }catch (Exception e) { e.printStackTrace(); }
        }else if (oldActive != setActive && newName.equalsIgnoreCase(oldName)) {
            try {
                controller.updateBillStatus(oldName, setActive);
                controller.loadBills();
                editStage.close();
                controller.popNewCombo(newBillchk.isSelected());
                newCombo.getSelectionModel().select(oldName);
            }catch (Exception e) { e.printStackTrace(); }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Modify Bill");
            alert.setContentText("Oops, that bill already exists! (or is invalid)");
            alert.showAndWait();
        }
    }

    private void delBill() {
        String newName = ((TextField) centerEditBill.getChildren().get(0)).getText().strip();
        String oldName = ((ComboBox) topNewBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Bill");
        alert.setHeaderText("Warning! Please read before continuing.");
        alert.setContentText("This will delete ALL ENTRIES for this bill. Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            if (!newName.equalsIgnoreCase(oldName)) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Security Error: Something doesn't add up (name match violation)");
                alert.setContentText("Please try deleting this entry again.");
                alert.showAndWait();
                editStage.close();
                return;
            }
            try {
                Connection conn = Launcher.getConnection();
                PreparedStatement statement = conn.prepareStatement("delete from bill where name=?");
                statement.setString(1, oldName);
                statement.executeUpdate();
                controller.loadBills();
                editStage.close();
                newCombo.getSelectionModel().clearSelection();
                controller.popNewCombo(newBillchk.isSelected());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void toggleBillEdit(boolean edit) {
        centerEditBill.getChildren().get(0).setDisable(!edit);
        centerEditBill.getChildren().get(1).setDisable(!edit);
        bottomEditBill.getChildren().get(0).setDisable(!edit);
        bottomEditBill.getChildren().get(1).setDisable(!edit);
    }

    //Search Stage helper methods
    private void search() {
        initSearchStage();
        controller.popSearchCombo(false);
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
        controller.entryPop(statement.toString());
    }

    protected void popSearchCombo(ObservableList<String> items) {
        searchCombo.setItems(items);
        searchCombo.getSelectionModel().selectFirst();
    }

    private void toggleDatePickers(DatePicker date, DatePicker date2) {
        date.setDisable(date.isDisabled()?false:true);
        date2.setDisable(date2.isDisabled()?false:true);
    }

    private void toggleAmountFields(TextField field, TextField field2) {
        field.setDisable(field.isDisabled()?false:true);
        field2.setDisable(field2.isDisabled()?false:true);
    }

    //New Entry Stage helper methods
    private void newEntry() {
        initNewStage();
        controller.popNewCombo(false);
        newStage.showAndWait();
    }

    protected void popNewCombo(ObservableList<String> items) {
        newCombo.setItems(items);
    }

    //More
    private void submitEntry() {
        String name = "";
        try {
            name = ((ComboBox) topNewBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
        }catch (Exception e) { }
        float amount = -1f;
        try {
            amount = Float.parseFloat( ((TextField) centerNewBox.getChildren().get(1)).getText());
        }catch (NumberFormatException e) { }

        if (name.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Add Entry");
            alert.setContentText("Oops, no bill is selected! Please select or create one");
            alert.showAndWait();
            return;
        }else if (amount < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Add Entry");
            alert.setContentText("Invalid amount");
            alert.showAndWait();
            return;
        }
        LocalDate date = ((DatePicker) centerNewBox.getChildren().get(0)).getValue();
        String notes = ((TextField) centerNewBox.getChildren().get(2)).getText();
        try {
            controller.insertNewEntry(name, date, amount, notes);
            newStage.close();
            controller.entryPop("select * from entry join bill on bill.name=entry.name where entry.name=\'"+name+"\' " +
                    "order by date desc");
        }catch (Exception e) { e.printStackTrace(); }
    }

    private void createBill() {
        TextInputDialog input = new TextInputDialog();
        input.setTitle("Create Bill");
        input.setHeaderText("Enter a bill name:");
        input.setContentText("Bill:");
        Optional<String> result = input.showAndWait();
        if (result.isPresent()) {
            String bill = result.get().strip();
            if (controller.verifyName(bill)) {
                try {
                    controller.insertNewBill(bill);
                    controller.loadBills();
                    controller.popNewCombo(newBillchk.isSelected());
                    newCombo.getSelectionModel().select(bill);
                }catch (Exception e) { e.printStackTrace(); }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot Create Bill");
                alert.setContentText("Oops, that bill already exists! (or is invalid)");
                alert.showAndWait();
            }
        }
    }

    private VBox genVBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15, 12, 15, 12));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.BASELINE_CENTER);
        return vbox;
    }

    private HBox genHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.BASELINE_CENTER);
        hbox.setStyle("-fx-background-color: #336699;");
        return hbox;
    }

    protected void popTView(ObservableList entries) {
        tview.refresh();
        tview.setItems(entries);
    }

    private void viewEntry() {
        try {
            tview.getSelectionModel().getSelectedItem();
        }catch (NullPointerException e) {
            System.out.println("Please select an entry");
            return;
        }
        System.out.println("Entry details");
        //Use column 0 to get ID, then request payment info from controller
        viewStage = new Stage();
    }

    private void stats() {
        System.out.println("Stats");
        statStage = new Stage();
    }

    private void pageLeft() {
        System.out.println("Page left");
    }

    private void pageRight() {
        System.out.println("Page right");
    }
}
