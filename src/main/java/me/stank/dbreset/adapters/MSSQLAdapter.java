package me.stank.dbreset.adapters;

import me.stank.dbreset.DBAdapter;
import me.stank.dbreset.Relationship;
import me.stank.dbreset.Table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class MSSQLAdapter implements DBAdapter {
    private static final char ESCAPE_CHAR = '"';

    @Override
    public Set<Table> getAllTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    "select s.name, t.name\n" +
                            "from sys.tables t\n" +
                            "INNER JOIN sys.schemas s ON t.schema_id = s.schema_id"
            );

            return Table.fromResultSet(rs);
        }
    }

    @Override
    public Set<Relationship> getAllRelationships(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    "select\n" +
                            "   fk_schema.name, so_fk.name,\n" +
                            "   pk_schema.name, so_pk.name,\n" +
                            "   sfk.name\n" +
                            "from\n" +
                            "sys.foreign_keys sfk\n" +
                            "\tinner join sys.objects so_pk on sfk.referenced_object_id = so_pk.object_id\n" +
                            "\tinner join sys.schemas pk_schema on so_pk.schema_id = pk_schema.schema_id\n" +
                            "\tinner join sys.objects so_fk on sfk.parent_object_id = so_fk.object_id\t\t\t\n" +
                            "\tinner join sys.schemas fk_schema on so_fk.schema_id = fk_schema.schema_id"
            );
            return Relationship.fromResultSet(rs);
        }
    }

    @Override
    public void delete(Connection connection, Set<Table> tables, Set<Relationship> cyclicRelationships) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (Relationship relationship : cyclicRelationships) {
                String sql = String.format("ALTER TABLE %s NOCHECK CONSTRAINT ALL;", relationship.getParent().getEscapedName(ESCAPE_CHAR));
                statement.addBatch(sql);
            }

            for (Table table : tables) {
                String sql = String.format("DELETE %s;", table.getEscapedName(ESCAPE_CHAR));
                statement.addBatch(sql);
            }

            for (Relationship relationship : cyclicRelationships) {
                String sql = String.format("ALTER TABLE %s WITH CHECK CHECK CONSTRAINT ALL;", relationship.getParent().getEscapedName(ESCAPE_CHAR));
                statement.addBatch(sql);
            }

            statement.executeBatch();
        }
    }
}
