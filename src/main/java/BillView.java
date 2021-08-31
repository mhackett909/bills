import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;

public class BillView {
    private Stage primaryStage, searchStage, newStage, editStage;
    private Stage viewStage, paymentStage, statStage;
    private VBox topSearchBox, leftSearchBox, rightSearchBox; //search window
    private VBox topNewBox, centerNewBox; //new entry window
    private VBox centerEditBill; //bill edit window
    private HBox bottomEditBill;
    private VBox leftvbox, midvbox, rightvbox; //payment window

    //Controls
    private TableView tview, pview;
    private ComboBox searchCombo, newCombo;
    private CheckBox newBillchk, searchchk;
    private Launcher controller;

    private int currentEntryID;

    public BillView(Launcher controller) {
        this.controller = controller;
    }

    //Primary Stage
    protected void initPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        HBox hbox = topHBox(), hbox2 = bottomHBox();
        tview = tView();
        pview = pView();

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
        TableColumn<BillData.Entry, Integer> column0 = new TableColumn<>("ID");
        column0.setCellValueFactory(new PropertyValueFactory<>("id"));
        column0.setVisible(false);

        TableColumn<BillData.Entry, String> column1 = new TableColumn<>("Name");
        column1.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<BillData.Entry, Date> column2 = new TableColumn<>("Date");
        column2.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<BillData.Entry, Float> column3 = new TableColumn<>("Amount");
        column3.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<BillView, ImageView> column4 = new TableColumn<>("Status");
        column4.setCellValueFactory(new PropertyValueFactory<>("image"));

        TableColumn<BillData.Entry, String> column5 = new TableColumn<>("Notes");
        column5.setCellValueFactory(new PropertyValueFactory<>("notes"));

        tView.getColumns().addAll(column0, column1, column2, column3, column4, column5);
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

        searchchk = new CheckBox("Include Inactive");
        searchchk.setTextFill(Color.WHITE);
        searchchk.setOnAction(event -> controller.popSearchCombo(searchchk.isSelected()));

        vbox.getChildren().addAll(searchCombo, searchchk);
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
            alert.setHeaderText("No Bill Selected");
            alert.setContentText("Please select or create one");
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

    //View Stage
    private void viewEntry() {
        TablePosition pos;
        try {
            pos = (TablePosition) tview.getSelectionModel().getSelectedCells().get(0);
        }catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No Entry Selected");
            alert.setContentText("Please select or create one");
            alert.showAndWait();
            return;
        }
        int row = pos.getRow();
        TableColumn column = (TableColumn) tview.getColumns().get(0);
        int id = Integer.parseInt(column.getCellObservableValue(row).getValue().toString());
        initViewStage();
        controller.paymentPop(id);
        viewStage.showAndWait();
    }

    private void initViewStage() {
        viewStage = new Stage();
        viewStage.initModality(Modality.WINDOW_MODAL);
        viewStage.initOwner(primaryStage);

        //Spacers
        Pane spacer = new Pane();
        spacer.setMinSize(20,1);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Pane spacer2 = new Pane();
        spacer2.setMinSize(20,1);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Pane spacer3 = new Pane();
        spacer3.setMinSize(1,20);
        VBox.setVgrow(spacer3, Priority.ALWAYS);

        Pane spacer4 = new Pane();
        spacer4.setMinSize(1,1);
        VBox.setVgrow(spacer4, Priority.ALWAYS);

        //HBox has 3 VBoxes
        leftvbox = genVBox();
        midvbox = genVBox();
        rightvbox = genVBox();

        //Left VBox
        Label entryName = new Label("Bill Name");
        entryName.setTextFill(Color.WHITE);
        entryName.setAlignment(Pos.CENTER);
        entryName.setPrefSize(150, 20);

        Label invoiceNum = new Label("Invoice #");

        Label entryStatus = new Label("Status");

        CheckBox editchk = new CheckBox("Edit");
        editchk.setOnAction(event -> toggleEntryEdit(editchk.isSelected()));
        editchk.setTextFill(Color.WHITE);

        leftvbox.getChildren().addAll(entryName, invoiceNum, entryStatus, editchk);

        //Middle VBox
        DatePicker date = new DatePicker();
        date.setDisable(true);

        TextField amount = new TextField();
        amount.setPromptText("Amount");
        amount.setDisable(true);

        TextField notes = new TextField();
        notes.setPromptText("Notes");
        notes.setDisable(true);

        midvbox.getChildren().addAll(date, amount, notes);

        //Right VBox
        Button del = new Button("Delete Entry");
        del.setPrefSize(100, 20);
        del.setOnAction(event -> delEntry());
        del.setTextFill(Color.RED);
        del.setDisable(true);

        Button add = new Button("Save");
        add.setPrefSize(100, 20);
        add.setOnAction(event -> saveEntry());
        add.setDisable(true);

        rightvbox.getChildren().addAll(del, add);

        HBox hbox = genHBox();
        hbox.getChildren().addAll(leftvbox, spacer, midvbox, spacer2, rightvbox);

        //VBox
        Label label = new Label("Due: ");
        label.setPrefSize(100, 20);
        label.setTextFill(Color.RED);
        label.setAlignment(Pos.CENTER);

        Button addP = new Button("Make Payment");
        addP.setPrefSize(100, 20);
        addP.setOnAction(event -> makePayment(true));

        Button delP = new Button("View Payment");
        delP.setPrefSize(100, 20);
        delP.setOnAction(event -> makePayment(false));

        VBox vbox = genVBox();
        vbox.getChildren().addAll(spacer4, label, addP, delP, spacer3);

        BorderPane border = new BorderPane();
        border.setTop(hbox);
        border.setCenter(vbox);
        border.setBottom(pview);

        viewStage.setScene(new Scene(border));
        viewStage.setTitle("Entry Details");
        viewStage.setMinHeight(300);
        viewStage.setMinWidth(600);
    }

    private TableView pView() {
        TableView pView = new TableView();

        TableColumn<BillData.Payment, Integer> column0 = new TableColumn<>("ID");
        column0.setCellValueFactory(new PropertyValueFactory<>("id"));
        column0.setVisible(false);

        TableColumn<BillData.Payment, Integer> column1 = new TableColumn<>("ID");
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));
        column1.setVisible(false);

        TableColumn<BillData.Payment, Date> column2 = new TableColumn<>("Date");
        column2.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<BillData.Payment, Float> column3 = new TableColumn<>("Amount");
        column3.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<BillData.Payment, String> column4 = new TableColumn<>("Type");
        column4.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<BillData.Payment, String> column5 = new TableColumn<>("Medium");
        column5.setCellValueFactory(new PropertyValueFactory<>("medium"));

        TableColumn<BillData.Payment, String> column6 = new TableColumn<>("Notes");
        column6.setCellValueFactory(new PropertyValueFactory<>("notes"));

        pView.getColumns().addAll(column0, column1, column2, column3, column4, column5, column6);
        pView.setPrefHeight(200);
        return pView;
    }

    //Edit Stage Helper Methods
    private void saveBill() {
        String newName = ((TextField) centerEditBill.getChildren().get(0)).getText().strip();
        String oldName = ((ComboBox) topNewBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
        boolean setActive = ((CheckBox) centerEditBill.getChildren().get(1)).isSelected();
        boolean oldActive = controller.getBillByName(oldName).isActive();
        if (newName.equals(oldName) && setActive==oldActive) {
            editStage.close();
            return;
        }
        if (controller.verifyName(newName) || newName.equalsIgnoreCase(oldName)){
            controller.renameBill(oldName, newName, setActive);
            controller.loadBills();
            editStage.close();
            controller.popNewCombo(newBillchk.isSelected());
            newCombo.getSelectionModel().select(newName);
            controller.resubmitLastQuery();
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Modify: Bill already exists");
            alert.setContentText("It may also be an in invalid name");
            alert.showAndWait();
        }
    }

    private void delBill() {
        String newName = ((TextField) centerEditBill.getChildren().get(0)).getText().strip();
        String oldName = ((ComboBox) topNewBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Bill");
        alert.setHeaderText("Warning! This will DELETE ALL ENTRIES.");
        alert.setContentText("Are you really sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            if (!newName.equalsIgnoreCase(oldName)) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Security Error: Name Match Violation)");
                alert.setContentText("Something doesn't add up. Please try deleting this entry again.");
                alert.showAndWait();
                editStage.close();
                return;
            }
            controller.delBill(oldName);
            controller.loadBills();
            editStage.close();
            newCombo.getSelectionModel().clearSelection();
            controller.resubmitLastQuery();
            controller.popNewCombo(newBillchk.isSelected());
        }
    }

    private void toggleBillEdit(boolean edit) {
        centerEditBill.getChildren().get(0).setDisable(!edit);
        centerEditBill.getChildren().get(1).setDisable(!edit);
        bottomEditBill.getChildren().get(0).setDisable(!edit);
        bottomEditBill.getChildren().get(1).setDisable(!edit);
    }

    //View stage helper methods
    private void makePayment(boolean newPayment) {
        System.out.println("New payment? "+newPayment);
    }

    private void delEntry() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Entry");
        alert.setHeaderText("Warning! This entry and its payments will be deleted.");
        alert.setContentText("Proceed?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            controller.delEntry(currentEntryID);
            viewStage.close();
            controller.resubmitLastQuery();
        }
    }

    private void saveEntry() {
        System.out.println("save entry");
        float amount = -1f;
        try {
            amount = Float.parseFloat(((TextField) midvbox.getChildren().get(1)).getText());
        }catch (NumberFormatException e) { }
        if (amount < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Amount");
            alert.setContentText("Remember no dollar sign needed");
            alert.showAndWait();
            return;
        }
        LocalDate date = ((DatePicker) midvbox.getChildren().get(0)).getValue();
        String notes = ((TextField) midvbox.getChildren().get(2)).getText();
        controller.saveEntry(currentEntryID, date, amount, notes);
        ((CheckBox) leftvbox.getChildren().get(3)).setSelected(false);
        toggleEntryEdit(false);
        controller.resubmitLastQuery();

    }

    protected void setEntry(String name, boolean status, int e_ID, Date date, float amount, String notes) {
        Tooltip tooltip = new Tooltip(name);
        tooltip.setStyle("-fx-font: normal bold 16 Tahoma;");

        Label entryLabel = (Label) leftvbox.getChildren().get(0);
        entryLabel.setText(name);
        entryLabel.setTooltip(tooltip);

        Label idLabel = (Label) leftvbox.getChildren().get(1);
        idLabel.setTextFill(Color.WHITE);
        idLabel.setText("Invoice #"+e_ID);
        currentEntryID = e_ID;

        Label statusLabel = (Label) leftvbox.getChildren().get(2);
        statusLabel.setText(status ? "Active" : "Inactive");
        statusLabel.setTextFill(status ? Color.LAWNGREEN : Color.ORANGE);
        statusLabel.setStyle("-fx-font-weight: bold;");

        ((DatePicker) midvbox.getChildren().get(0)).setValue(date.toLocalDate());
        ((TextField) midvbox.getChildren().get(1)).setText(Float.toString(amount));
        ((TextField) midvbox.getChildren().get(2)).setText(notes);

    }

    private void toggleEntryEdit(boolean edit) {
        for (int x = 0; x < midvbox.getChildren().size(); x++)
            midvbox.getChildren().get(x).setDisable(!edit);
        for (int x = 0; x < rightvbox.getChildren().size(); x++)
            rightvbox.getChildren().get(x).setDisable(!edit);
    }

    //Search Stage helper methods
    private void search() {
        initSearchStage();
        controller.popSearchCombo(false);
        searchStage.showAndWait();
    }

    private void searchEntry() {
        String bill = ((ComboBox) topSearchBox.getChildren().get(0)).getSelectionModel().getSelectedItem().toString();
        boolean showInactive = ((CheckBox) topSearchBox.getChildren().get(1)).isSelected();
        StringBuilder statement = new StringBuilder();
        statement.append("select * from entry join bill on bill.name=entry.name where");

        if (!showInactive) statement.append(" bill.status=1 and");

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
            alert.setHeaderText("No Bill Selected");
            alert.setContentText("Please select or create one");
            alert.showAndWait();
            return;
        }else if (amount < 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Amount");
            alert.setContentText("Remember no dollar sign needed");
            alert.showAndWait();
            return;
        }
        LocalDate date = ((DatePicker) centerNewBox.getChildren().get(0)).getValue();
        String notes = ((TextField) centerNewBox.getChildren().get(2)).getText();
        controller.insertNewEntry(name, date, amount, notes);
        newStage.close();
        controller.entryPop("select * from entry join bill on bill.name=entry.name where entry.name=\'"+name+"\' " +
                    "order by date desc");
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
                controller.insertNewBill(bill);
                controller.loadBills();
                controller.popNewCombo(newBillchk.isSelected());
                newCombo.getSelectionModel().select(bill);
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot create: Bill already exists");
                alert.setContentText("Its name may also be invalid. Perhaps include inactive?");
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

    protected void popPView(ObservableList entries) {
        pview.refresh();
        pview.setItems(entries);
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
