import java.sql.Date;
import java.util.ArrayList;

public class Entry {
    ArrayList<Payment> payments;
    int id;
    String name;
    Date date;
    float amount;
    int status;
    String notes;
    Entry(int id, String name, Date date, float amount,
          int status, String n) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.status = status;
        notes = n;
    }
    public void addPayment(Payment payment) {
            //Check for duplicate?
            payments.add(payment);
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public Date getDate() { return date; }
    public float getAmount() { return amount; }
    public int getStatus() { return status; }
    public String getNotes() { return notes; }


    private class Payment {
        int id;
        int entryID;
        Date date;
        float amount;
        String type;
        String notes;
        Payment(int id, int eID, Date date, float a, String t, String n) {
            this.id = id;
            entryID = eID;
            this.date = date;
            amount = a;
            type = t;
            notes = n;
        }
    }
}

