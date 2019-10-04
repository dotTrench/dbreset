package me.stank.dbreset;

import me.stank.dbreset.adapters.PostgreSQLAdapter;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Testcontainers
public class PostgreSQLTests implements DBAdapterTest {
    private static final String DOCKER_IMAGE = "postgres:11-alpine";

    @Container
    private final PostgreSQLContainer container = new PostgreSQLContainer(DOCKER_IMAGE);

    @Override
    public DBAdapter adapter() {
        return new PostgreSQLAdapter();
    }

    @Override
    public Connection connection() throws SQLException {
        String url = container.getJdbcUrl();
        return DriverManager.getConnection(url, container.getUsername(), container.getPassword());
    }
}
