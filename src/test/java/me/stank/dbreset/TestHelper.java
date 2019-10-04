package me.stank.dbreset;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class TestHelper {
    public static int count(Connection connection, String table) throws SQLException {
        Statement statement = connection.createStatement();
        try (ResultSet res = statement.executeQuery("select count(*) as total from " + table)) {
            res.next();
            return res.getInt(1);
        }
    }

    public static void runMigration(Connection connection, String name) throws Exception {
        String sql = readResourceAsString(name);

        String[] parts = sql.split("\r?\n");
        try (Statement statement = connection.createStatement()) {
            for (String part : parts) {
                statement.addBatch(part);
            }

            statement.executeBatch();
        }
    }

    static void execute(Connection connection, String sql) throws SQLException {
        connection.createStatement().execute(sql);
    }

    private static String readResourceAsString(String name) throws URISyntaxException, IOException {
        Path path = Paths.get(Objects.requireNonNull(TestHelper.class.getClassLoader().getResource(name)).toURI());

        return new String(Files.readAllBytes(path));
    }
}
