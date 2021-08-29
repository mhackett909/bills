import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class Launcher extends Application {
    private BillView billView;
    private BillData billData;

    @Override
    public void start(Stage primaryStage) {
        billView = new BillView(this);
        billData = new BillData();
        billView.initPrimaryStage(primaryStage);
        loadBills();
        entryPop("select * from entry " +
                "join bill on bill.name=entry.name " +
                "where date >= DATE_SUB(NOW(), INTERVAL 90 DAY) " +
                "order by date desc");
        primaryStage.show();
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setAlwaysOnTop(false);
    }

    //Need this if we want to pass in command-line args
    //Also, makes for a clear explicit launch point
    public static void main(String[] args) {
        Application.launch(args);
    }

    //Controller methods
    protected void renameBill(String oldName, String newName, boolean setActive) throws SQLException {
        Connection conn = Launcher.getConnection();
        PreparedStatement statement = conn.prepareStatement("update bill set name=?, status=? where name=?");
        statement.setString(1, newName);
        statement.setBoolean(2, setActive);
        statement.setString(3, oldName);
        statement.executeUpdate();
    }

    protected void updateBillStatus(String name, boolean setActive) throws SQLException {
        Connection conn = Launcher.getConnection();
        PreparedStatement statement = conn.prepareStatement("update bill set status=? where name=?");
        statement.setBoolean(1, setActive);
        statement.setString(2, name);
        statement.executeUpdate();

    }

    protected BillData.Bill getBillByName(String name) {
        for (BillData.Bill bill : billData.getBills()) {
            if (bill.getName().equals(name)) return bill;
        }
        return null;
    }

    protected void popSearchCombo(boolean all) {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (BillData.Bill bill : billData.getBills()) {
            if (bill.isActive() || all) items.add(bill.getName());
        }
        billView.popSearchCombo(items);
    }

    protected void popNewCombo(boolean all) {
        ObservableList<String> items = FXCollections.observableArrayList();
        for (BillData.Bill bill : billData.getBills()) {
            if (bill.getName().equals("All Bills")) continue;
            if (bill.isActive() || all) items.add(bill.getName());
        }
        billView.popNewCombo(items);
    }

    protected void insertNewEntry(String name, LocalDate date, float amount, String notes) throws SQLException {
        Connection conn = Launcher.getConnection();
        PreparedStatement statement = conn.prepareStatement("INSERT INTO entry(name, date, amount, status, services) values(?,?,?,?,?)");
        statement.setString(1, name);
        statement.setDate(2, Date.valueOf(date));
        statement.setFloat(3, amount);
        statement.setInt(4, 0);
        statement.setString(5, notes);
        statement.executeUpdate();
    }

    protected void insertNewBill(String bill) throws SQLException {
        Connection conn = Launcher.getConnection();
        PreparedStatement statement = conn.prepareStatement("INSERT INTO bill(name, status) values(?,?)");
        statement.setString(1, bill);
        statement.setBoolean(2, true);
        statement.executeUpdate();
    }
    protected boolean verifyName(String newBill) {
        newBill = newBill.toLowerCase();
        if (newBill.equals("") || newBill.equals("all bills")) return false;
        String oldBill;
        for (BillData.Bill bill : billData.getBills()) {
            oldBill = bill.getName().toLowerCase();
            if (oldBill.equals("all bills")) continue;
            if (oldBill.equals(newBill)) return false;
        }
        return true;
    }

    protected void loadBills() {
        try {
            billData.initBills();
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("select * from bill");
            ResultSet rs = ps.executeQuery();
            billData.addBill("All Bills", true);
            while(rs.next()) {
                billData.addBill(rs.getString(1), rs.getBoolean(2));
            }
        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void entryPop(String statement) {
        try {
            billData.initEntries();
            Connection conn = getConnection();
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
                billData.addEntry(id, name, date, amount, status, notes);
            }
            billView.popTView(billData.getEntries());
        }catch (SQLException t) { t.printStackTrace(); }
    }

    //Database connection
    protected static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/bills";
        String username = "root";
        String password = "password";
        return DriverManager.getConnection(url, username, password);
    }
}
