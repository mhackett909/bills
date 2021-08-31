import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class Launcher extends Application {
    private BillView billView;
    private BillData billData;
    private String lastQuery;

    @Override
    public void start(Stage primaryStage) {
        billView = new BillView(this);
        billData = new BillData();
        billView.initPrimaryStage(primaryStage);
        loadBills();
        entryPop("select * from entry " +
                "join bill on bill.name=entry.name " +
                "where date >= DATE_SUB(NOW(), INTERVAL 90 DAY) " +
                "and bill.status=1 " +
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
    protected void renameBill(String oldName, String newName, boolean setActive) {
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("update bill set name=?, status=? where name=?");
            statement.setString(1, newName);
            statement.setBoolean(2, setActive);
            statement.setString(3, oldName);
            statement.executeUpdate();
        }catch (SQLException t) { t.printStackTrace();  }
        for (BillData.Bill bill : billData.getBills()) {
            if (bill.getName().equalsIgnoreCase(oldName)) {
                bill.setName(newName);
                bill.setActive(setActive);
                break;
            }
        }
        for (BillData.Entry entry : billData.getEntries()) {
            if (entry.getName().equalsIgnoreCase(oldName)) {
                entry.setName(newName);
                break;
            }
        }
    }

    protected BillData.Bill getBillByName(String name) {
        for (BillData.Bill bill : billData.getBills()) {
            if (bill.getName().equals(name)) return bill;
        }
        return null;
    }

    protected BillData.Entry getEntryByID(int id) {
        for (BillData.Entry entry : billData.getEntries()) {
            if (entry.getId() == id) return entry;
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

    protected void insertNewEntry(String name, LocalDate date, float amount, String notes) {
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("INSERT INTO entry(name, date, amount, status, services) values(?,?,?,?,?)");
            statement.setString(1, name);
            statement.setDate(2, Date.valueOf(date));
            statement.setFloat(3, amount);
            statement.setInt(4, 0);
            statement.setString(5, notes);
            statement.executeUpdate();
        } catch (SQLException t) { t.printStackTrace();  }
    }

    protected void insertNewBill(String bill) {
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("INSERT INTO bill(name, status) values(?,?)");
            statement.setString(1, bill);
            statement.setBoolean(2, true);
            statement.executeUpdate();
        }catch (SQLException t) { t.printStackTrace();  }
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
            lastQuery = statement;
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

    protected void paymentPop(int id) {
        try {
            billData.initPayments();
            String statement = "select * from entry join payment on entry.id=payment.entryID where entry.id=?";
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
               int paymentID = rs.getInt(7);
               int entryID = rs.getInt(8);
               Date date = rs.getDate(9);
               float amount = rs.getFloat(10);
               String type = rs.getString(11);
               String medium = rs.getString(12);
               String notes = rs.getString(13);
               billData.addPayment(paymentID, entryID, date, amount, type, medium, notes);
            }
            BillData.Entry entry = getEntryByID(id);
            BillData.Bill bill = getBillByName(entry.getName());
            billView.setEntry(bill.getName(), bill.isActive(), id, entry.getDate(), entry.getAmount(), entry.getNotes());
            billView.popPView(billData.getPayments());
        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void saveEntry(int id, LocalDate date, float amount, String notes) {
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("update entry set date=?, amount=?, services=? where id=?");
            statement.setDate(1, Date.valueOf(date));
            statement.setFloat(2, amount);
            statement.setString(3, notes);
            statement.setInt(4, id);
            statement.executeUpdate();

        }catch(SQLException t) { t.printStackTrace(); }
    }

    protected void resubmitLastQuery() {
        //System.out.println("exec: "+lastQuery);
        entryPop(lastQuery);
    }

    protected void delBill(String name) {
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement("delete from bill where name=?");
            statement.setString(1, name);
            statement.executeUpdate();
        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void delEntry(int id) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("delete from entry where entry.id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
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
