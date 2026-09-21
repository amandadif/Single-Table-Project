package datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


class DatabaseRegistry
{
    public static final String DB_URL = "jdbc:sqlite:products.sqlite";
    private static Connection connection;

    static Connection getConnection() throws SQLException
    {
        if (connection == null || connection.isClosed())
        {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(false);
        }
        assert !connection.isClosed();
        return connection;
    }

}