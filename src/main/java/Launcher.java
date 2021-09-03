import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

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
        reset();
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
    protected void reset() {
        entryPop("select * from entry " +
                "join bill on bill.name=entry.name " +
                "where date >= DATE_SUB(NOW(), INTERVAL 90 DAY) " +
                "and bill.status=1 " +
                "order by date desc");
    }
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

    protected BillData.Payment getPaymentByID(int id) {
        for (BillData.Payment payment : billData.getPayments()) {
            if (payment.getId() == id) return payment;
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
            billData.addBill("All Bills", true);
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("select * from bill");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                billData.addBill(rs.getString(1), rs.getBoolean(2));
            }
        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void loadPaymentMethods() {
        ObservableList<String> methods = FXCollections.observableArrayList();
        methods.addAll("ACH (Direct) Debit", "Cash", "Check", "Credit", "Cryptocurrency");
        methods.addAll("Debit", "Electronic Bank Transfer", "eWallet (Apple Pay, Google Pay...)");
        methods.addAll("Gift Card", "Prepaid Card", "Service (Zelle, Venmo, PayPal...)", "Other");

        ObservableList<String> mediums = FXCollections.observableArrayList();
        mediums.addAll("App", "Automatic Payment", "Cash-on-Delivery", "In-person", "Mail");
        mediums.addAll("Website", "Telephone", "Other");

        billView.popMethodCombo(methods);
        billView.popMediumCombo(mediums);
    }

    protected void popPaymentWindow(int currentPaymentID) {
        Date date = null;
        float amount = 0;
        String method, medium, notes;
        method = medium = notes = "";
        for (BillData.Payment payment : billData.getPayments()) {
            if (payment.getId() == currentPaymentID) {
                date = payment.getDate();
                amount = payment.getAmount();
                method = payment.getMethod();
                medium = payment.getMedium();
                notes = payment.getNotes();
                break;
            }
        }
        billView.popPaymentWindow(date, amount, method, medium, notes);
    }

    protected void delPayment(int paymentID, int entryID, float amountDue) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("delete from payment where id=?");
            ps.setInt(1,paymentID);
            ps.executeUpdate();
            BillData.Payment payment = getPaymentByID(paymentID);
            float deletedAmount = payment.getAmount();
            amountDue+=deletedAmount;
            updateEntryStatus(entryID, amountDue);

        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void insertPayment(boolean newPayment, int currentEntryID, int currentPaymentID, float amountDue,
                                 Date date, float amount, String mthd, String mdum, String notes) {
        try {
            Connection conn = getConnection();
            PreparedStatement ps;
            String statement;
            if (newPayment) {
                statement = "insert into payment(entryID, date, amount, type, medium, notes) " +
                        "values(?,?,?,?,?,?)";
                ps = conn.prepareStatement(statement);
                ps.setInt(1, currentEntryID);
                ps.setDate(2, date);
                ps.setFloat(3, amount);
                ps.setString(4, mthd);
                ps.setString(5, mdum);
                ps.setString(6, notes);
            }
            else {
                statement = "update payment set date=?, amount=?, type=?, medium=?, notes=? where id=?";
                ps = conn.prepareStatement(statement);
                ps.setDate(1, date);
                ps.setFloat(2, amount);
                ps.setString(3, mthd);
                ps.setString(4, mdum);
                ps.setString(5, notes);
                ps.setInt(6, currentPaymentID);
            }
            ps.executeUpdate();
            if (newPayment) amountDue-=amount;
            else {
                BillData.Payment payment = getPaymentByID(currentPaymentID);
                float oldAmount = payment.getAmount();
                float difference = amount - oldAmount;
                amountDue-=difference;
            }
            updateEntryStatus(currentEntryID, amountDue);
        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void updateEntryStatus(int id, float amountDue) {
        int status;
        if (amountDue < 0) status = 2;
        else if (amountDue == 0) status = 1;
        else status = 0;
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("update entry set status=? where id=?");
            ps.setInt(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }catch (SQLException t) { t.printStackTrace(); }
    }

    protected void entryPop(String statement) {
        Task execTask = new Task() {
            @Override
            protected Object call() {
                try {
                    billData.initEntries();
                    Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(statement);
                    //System.out.println("Exec: "+statement);
                    lastQuery = statement;
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String name = rs.getString(2);
                        Date date = rs.getDate(3);
                        float amount = rs.getFloat(4);
                        int status = rs.getInt(5);
                        String notes = rs.getString(6);
                        billData.addEntry(id, name, date, amount, status, notes);
                    }
                } catch (SQLException t) {
                    t.printStackTrace();
                }
                updateProgress(1,1);
                return null;
            }
        };
        execTask.setOnScheduled(e -> billView.progressBar(true, execTask.progressProperty()));
        execTask.setOnFailed(e -> billView.progressBar(false, execTask.progressProperty()));
        execTask.setOnCancelled(e -> billView.progressBar(false, execTask.progressProperty()));
        execTask.setOnSucceeded(e -> billView.progressBar(false, execTask.progressProperty()));
        new Thread(execTask).start();
    }

    protected void popTView() {
        billView.popTView(billData.getEntries());
    }

    protected void paymentPop(int id) {
        try {
            billData.initPayments();
            String statement = "select * from entry join payment on entry.id=payment.entryID where entry.id=?";
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(statement);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            float totalPaid = 0, totalDue;
            while (rs.next()) {
               int paymentID = rs.getInt(7);
               int entryID = rs.getInt(8);
               Date date = rs.getDate(9);
               float amount = rs.getFloat(10);
               String type = rs.getString(11);
               String medium = rs.getString(12);
               String notes = rs.getString(13);
               billData.addPayment(paymentID, entryID, date, amount, type, medium, notes);
               totalPaid+=amount;
            }
            BillData.Entry entry = getEntryByID(id);
            BillData.Bill bill = getBillByName(entry.getName());
            billView.setEntry(bill.getName(), bill.isActive(), id, entry.getDate(), entry.getAmount(), entry.getNotes());
            billView.popPView(billData.getPayments());
            totalDue = entry.getAmount()-totalPaid;
            billView.setDueLabel(totalDue);
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

    protected void stats() {
        try {
            //TODO Note that this solution only allows 65,535 entries... (two bytes max num_params for prepared statements)
            ArrayList<String> list = new ArrayList<>();
            for (BillData.Entry entry : billData.getEntries()) {
                list.add(Integer.toString(entry.getId()));
            }
            if (list.size() == 0) list.add("-1"); //dummy value

            String fullList="";
            for (String next : list) fullList += next + ",";
            fullList = fullList.substring(0, fullList.length() - 1);

            int invoiceCount = 0;
            float totalBilled, totalPaid, avgBill, avgPay, highBill, highPay, totalDue, totalOverpaid;
            totalBilled = totalPaid = avgBill = avgPay = highBill = highPay = totalDue = totalOverpaid = 0;

            String stats = "select count(e.name) as InvoiceCount, sum(e.amount) as TotalBilled, avg(e.amount) as AverageBill, " +
                    "sum(p.amount) as TotalPaid, avg(p.amount) as AveragePaid, max(e.amount) as HighestBill, " +
                    "max(p.amount) as HighestPayment from entry e left join payment p on e.id=p.entryid " +
                    "where e.id in ("+fullList+")";

            String due = "select (sum(e.amount) - ifnull(sum(p.amount),0)) as TotalDue from entry e left join payment p " +
                    "on e.id=p.entryID where status=0 and e.id in ("+fullList+")";

            String overpaid = "select (sum(p.amount)-sum(e.amount)) as TotalOverpaid from entry e left join payment " +
                    "p on e.id=p.entryID where status=2 and e.id in ("+fullList+")";

            Connection conn = getConnection();
            PreparedStatement ps1 = conn.prepareStatement(stats);
            ResultSet rs1=ps1.executeQuery();
            while (rs1.next()) {
                invoiceCount = rs1.getInt(1);
                totalBilled = rs1.getFloat(2);
                avgBill = rs1.getFloat(3);
                totalPaid = rs1.getFloat(4);
                avgPay = rs1.getFloat(5);
                highBill = rs1.getFloat(6);
                highPay = rs1.getFloat(7);

            }
            PreparedStatement ps2 = conn.prepareStatement(due);
            ResultSet rs2=ps2.executeQuery();
            while (rs2.next()) {
                totalDue = rs2.getFloat(1);
            }
            PreparedStatement ps3 = conn.prepareStatement(overpaid);
            ResultSet rs3=ps3.executeQuery();
            while (rs3.next()) {
                totalOverpaid = rs3.getFloat(1);
            }
            billView.setStats(invoiceCount, totalBilled, totalPaid, avgBill, avgPay, highBill, highPay, totalDue, totalOverpaid);

        } catch (SQLException t) { t.printStackTrace();  }
    }

    //Database connection
    protected static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/bills";
        String username = "root";
        String password = "password";
        return DriverManager.getConnection(url, username, password);
    }
}
