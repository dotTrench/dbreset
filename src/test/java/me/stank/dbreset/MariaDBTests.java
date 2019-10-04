package me.stank.dbreset;

import me.stank.dbreset.adapters.MySQLAdapter;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Testcontainers
public class MariaDBTests implements DBAdapterTest {
    @Container
    private final MariaDBContainer container = new MariaDBContainer();

    @Override
    public DBAdapter adapter() {
        return new MySQLAdapter();
    }

    @Override
    public Connection connection() throws SQLException {
        return DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
    }
}
