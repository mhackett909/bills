import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class BillConverter {
    ArrayList<Bill> bills;
    PreparedStatement statement, statement2, statement3, statement4;
    public static void main(String[] args) { new BillConverter(); }

    public BillConverter() {
        bills = new ArrayList<>();
        String file1 = "C:\\Users\\Michael\\Desktop\\Archive.xml";
        String file2 = "C:\\Users\\Michael\\Desktop\\Bills.xml";
        loadXML(file1);
        loadXML(file2);
        writeToDatabase();
        //printBills();
    }
    static Connection getConnection() throws Exception {
        Connection conn = null;
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/bills";
        String username = "root";
        String password = ""; //TODO DEL PW
        //Class.forName(driver);
        conn = DriverManager.getConnection(url, username, password);
        System.out.println("connected");
        return conn;
    }

    //https://stackoverflow.com/questions/428073/what-is-the-best-simplest-way-to-read-in-an-xml-file-in-java-application
    void loadXML(String file) {
        try {
            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Bill");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String name = eElement.getAttribute("name");
                    int index = getBillIndex(name.toLowerCase());
                    Bill bill;
                    if (index == -1) {
                        bill = new Bill(name);
                        bills.add(bill);
                    }else bill = bills.get(index);
                    for (int x = 0; x <  eElement.getElementsByTagName("Entry").getLength(); x++) {
                        String entryString = eElement.getElementsByTagName("Entry")
                                .item(x).getTextContent();
                        String[] splitEntry = entryString.split("      ");
                        Entry entry = new Entry(splitEntry[1].strip(), splitEntry[2].strip(),
                                splitEntry[3].strip(), splitEntry[4].strip());
                        if (!duplicateEntry(bill, entry)) bill.entries.add(entry);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void printBills() {
        for (int x = 0; x < bills.size(); x++) {
            Bill bill = bills.get(x);
            System.out.println(bill.name + " : "+bill.entries.size());
            for (int y = 0; y < bill.entries.size(); y++) {
                System.out.println();
                System.out.println(bill.entries.get(y).date);
                System.out.println(bill.entries.get(y).amount);
                System.out.println(bill.entries.get(y).paid);
                System.out.println(bill.entries.get(y).notes);
            }
            System.out.println();
        }
    }
    int getBillIndex(String name) {
        int index = -1;
        for (int x = 0; x < bills.size(); x++) {
            if (bills.get(x).name.toLowerCase().equals(name)) return x;
        }
        return index;
    }
    boolean duplicateEntry(Bill bill, Entry entry) {
        boolean dup = false;
        for (int x = 0; x < bill.entries.size(); x++) {
            Entry nextEntry = bill.entries.get(x);
            if (entry.amount.equals(nextEntry.amount) && entry.date.equals(nextEntry.date)) {
                dup = true;
                if (!entry.paid.equals(nextEntry.paid) || !entry.notes.equals(nextEntry.notes))
                    dup = false; //"Soft duplicate" can pass through
                break;
            }
        }
        return dup;
    }
    void writeToDatabase() {
        try {
            Connection conn = getConnection();
            for (int x = 0; x < bills.size(); x++) {
                String bill = bills.get(x).name;
                statement = conn.prepareStatement("INSERT INTO bill(name, status) values(?,?)");
                statement.setString(1, bill);
                statement.setInt(2,1);
                //System.out.println(statement.toString());
                statement.executeUpdate();
                ArrayList entries = bills.get(x).entries;
                for (int y = 0; y < entries.size(); y++) {
                    statement2 = conn.prepareStatement("INSERT INTO entry(name, date, amount, paid, overpaid, services) " +
                            "values(?, ?, ?, ?, ?, ?)");
                    statement2.setString(1, bill);
                    Entry entry = (Entry) entries.get(y);
                    double amount = Double.parseDouble(entry.amount);
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                    java.util.Date parsed = format.parse(entry.date);
                    java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
                    statement2.setDate(2, sqlDate);
                    statement2.setDouble(3, amount);
                    boolean isPaid = !entry.paid.equals("Unpaid");
                    if (!isPaid) {
                        statement2.setInt(4, 0);
                        statement2.setString(6, entry.notes);
                    }
                    else {
                        statement2.setInt(4, 1);
                        statement2.setString(6, "");
                    }
                    statement2.setInt(5, 0);
                    //System.out.println(statement2.toString());
                    statement2.executeUpdate();
                    if (isPaid) {
                        statement3 = conn.prepareStatement("select last_insert_id()");
                        ResultSet rs = statement3.executeQuery();
                        rs.next();
                        int id = rs.getInt(1);
                        statement4 = conn.prepareStatement("INSERT INTO payment(entryID, date, amount, type, notes) " +
                                "values(?, ?, ?, ?, ?)");
                        statement4.setInt(1, id);
                        statement4.setDate(2, sqlDate);
                        statement4.setDouble(3, amount);
                        statement4.setString(4, entry.paid);
                        statement4.setString(5, entry.notes);
                        //System.out.println(statement4.toString());
                        statement4.executeUpdate();
                    }
                }
            }


        } catch (Exception throwables) {
            throwables.printStackTrace();
        }

    }
    class Bill {
        String name;
        ArrayList<Entry> entries;
        Bill(String name) {
            this.name = name;
            entries = new ArrayList<>();
        }
    }
    class Entry {
        String date;
        String amount;
        String paid;
        String notes;
        Entry(String date, String amount, String paid, String notes) {
            this.date = date;
            this.amount = amount;
            this.paid = paid;
            this.notes = notes;
        }
    }
}
