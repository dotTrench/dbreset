package me.stank.dbreset;

import me.stank.dbreset.adapters.MySQLAdapter;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Testcontainers
public class MySQLTests implements DBAdapterTest {
    @Container
    private final MySQLContainer container = new MySQLContainer();

    @Override
    public DBAdapter adapter() {
        return new MySQLAdapter();
    }

    @Override
    public Connection connection() throws SQLException {
        return DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
    }
}
