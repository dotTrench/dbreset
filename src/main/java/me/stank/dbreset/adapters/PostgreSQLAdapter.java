package me.stank.dbreset.adapters;

import me.stank.dbreset.DBAdapter;
import me.stank.dbreset.Relationship;
import me.stank.dbreset.Table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class PostgreSQLAdapter implements DBAdapter {
    private static final char ESCAPE_CHAR = '"';

    @Override
    public Set<Table> getAllTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("select TABLE_SCHEMA, TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_TYPE = 'BASE TABLE' and table_schema not in ('pg_catalog', 'information_schema')");

            return Table.fromResultSet(rs);
        }
    }

    @Override
    public Set<Relationship> getAllRelationships(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    "select tc.table_schema, tc.table_name, ctu.table_schema, ctu.table_name, rc.constraint_name\n" +
                            "from INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc\n" +
                            "inner join INFORMATION_SCHEMA.CONSTRAINT_TABLE_USAGE ctu ON rc.constraint_name = ctu.constraint_name\n" +
                            "inner join INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc ON rc.constraint_name = tc.constraint_name"
            );

            return Relationship.fromResultSet(rs);
        }
    }

    @Override
    public void delete(Connection connection, Set<Table> toDelete, Set<Relationship> cyclicRelationships) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (Relationship relationship : cyclicRelationships) {
                String sql = String.format("ALTER TABLE %s DISABLE TRIGGER ALL;", relationship.getParent().getEscapedName(ESCAPE_CHAR));
                statement.addBatch(sql);
            }

            for (Table table : toDelete) {
                String sql = String.format("TRUNCATE TABLE %s cascade;", table.getEscapedName(ESCAPE_CHAR));
                statement.addBatch(sql);
            }

            for (Relationship relationship : cyclicRelationships) {
                Table parent = relationship.getParent();
                String sql = String.format("ALTER TABLE %s ENABLE TRIGGER ALL;", parent.getEscapedName(ESCAPE_CHAR));
                statement.addBatch(sql);
            }
            statement.executeBatch();
        }
    }
}
