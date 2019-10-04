package me.stank.dbreset.adapters;

import me.stank.dbreset.DBAdapter;
import me.stank.dbreset.Relationship;
import me.stank.dbreset.Table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class MySQLAdapter implements DBAdapter {
    private static final char ESCAPE_CHAR = '`';

    @Override
    public Set<Table> getAllTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    "SELECT t.TABLE_SCHEMA, t.TABLE_NAME\n" +
                            "FROM\n" +
                            "    information_schema.tables AS t\n" +
                            "WHERE\n" +
                            "        table_type = 'BASE TABLE'\n" +
                            "  AND TABLE_SCHEMA NOT IN ('mysql' , 'performance_schema', 'sys')"
            );
            return Table.fromResultSet(rs);
        }
    }

    @Override
    public Set<Relationship> getAllRelationships(Connection connection) {
        // Not used in deletion
        return new HashSet<>();
    }

    @Override
    public void delete(Connection connection, Set<Table> tables, Set<Relationship> cyclicRelationships) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            statement.addBatch("SET FOREIGN_KEY_CHECKS=0;");
            for (Table table : tables) {
                String format = String.format("DELETE FROM %s;", table.getEscapedName(ESCAPE_CHAR));
                statement.addBatch(format);
            }

            statement.addBatch("SET FOREIGN_KEY_CHECKS=1;");

            statement.executeBatch();
        }
    }
}
