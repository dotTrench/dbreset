package me.stank.dbreset;

import me.stank.dbreset.adapters.MSSQLAdapter;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Testcontainers
public class MSSQLTests implements DBAdapterTest {
    @Container
    private final MSSQLServerContainer container = new MSSQLServerContainer();

    @Override
    public DBAdapter adapter() {
        return new MSSQLAdapter();
    }

    @Override
    public Connection connection() throws SQLException {
        String jdbcUrl = container.getJdbcUrl();
        return DriverManager.getConnection(jdbcUrl, container.getUsername(), container.getPassword());
    }
}
