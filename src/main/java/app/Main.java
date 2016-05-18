package app;

import org.h2.tools.RunScript;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./src/main/resources/Database")) {
            loadInitialData(connection);

            System.out.println("before:");
            dumpTable(connection);

            // account name could come from anywhere:
            // - JavaFX input
            // - command line argument
            // - over the network
            String depositToAccount = "Märt";
            //String depositToAccount = "Märt' or holder_name != 'Märt";
      /*String depositToAccount = "Taavi'; insert into accounts (holder_name, balance) values ('Z',1000);" +
              "update accounts SET balance = 0 WHERE holder_name = 'Märt';" +
              "DELETE FROM accounts WHERE holder_name = 'Taavi';" +
              "update accounts SET balance = 0 WHERE holder_name = 'Märt;";*/
            deposit10(connection, depositToAccount);
            System.out.println();
            System.out.println("after:");
            dumpTable(connection);
        }
    }

    private static void deposit10(Connection conn, String holderName) throws SQLException {
        String query = "UPDATE accounts SET balance = balance + 10 WHERE holder_name = ?;";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1,holderName);
            statement.executeUpdate();
        }
    }

    private static void loadInitialData(Connection connection) throws SQLException, IOException {
        try (Reader reader = new InputStreamReader(
                Main.class.getClassLoader().getResourceAsStream("database-setup.sql"), "UTF-8")) {
            RunScript.execute(connection, reader);
        }
    }

    private static void dumpTable(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * from accounts")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("holder_name") + "\t" + rs.getBigDecimal("balance"));
            }
        }
    }
}
