package me.stank.dbreset;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class Checkpoint {
    private final DBAdapter adapter;

    public Checkpoint(DBAdapter adapter) {
        this.adapter = adapter;
    }

    public void reset(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            reset(connection);
        }
    }

    public void reset(Connection connection) throws SQLException {
        Set<Table> allTables = adapter.getAllTables(connection);
        if (allTables.isEmpty()) return;

        Set<Relationship> relationships = adapter.getAllRelationships(connection);

        adapter.delete(connection, allTables, relationships);
    }
}
