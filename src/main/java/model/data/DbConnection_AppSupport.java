package model.data;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection_AppSupport {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = DbConnection_AppSupport.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (in != null) {
                props.load(in);
            } else {
                System.err.println("ERROR: db.properties no encontrado.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUrl() {
        return props.getProperty("DB_URL");
    }

    public static String getUser() {
        return props.getProperty("DB_USER");
    }

    public static String getPass() {
        return props.getProperty("DB_PASS");
    }

    public static Connection getConnection()
            throws SQLException, ClassNotFoundException {

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        return DriverManager.getConnection(
                getUrl(),
                getUser(),
                getPass()
        );
    }
}