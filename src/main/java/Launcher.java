import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Launcher extends Application {
    private BillView billView;

    @Override
    public void start(Stage primaryStage) {
        billView = new BillView();
        billView.initPrimaryStage(primaryStage);
        billView.loadBills();
        billView.entryPop("select * from entry " +
                "join bill on bill.name=entry.name " +
                "where date >= DATE_SUB(NOW(), INTERVAL 90 DAY) " +
                "order by date desc");
        billView.popTView();
        primaryStage.show();
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setAlwaysOnTop(false);
    }

    //Need this if we want to pass in command-line args
    //Also, makes for a clear explicit launch point
    public static void main(String[] args) {
        Application.launch(args);
    }

    protected static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/bills";
        String username = "root";
        String password = "password";
        return DriverManager.getConnection(url, username, password);
    }
}
