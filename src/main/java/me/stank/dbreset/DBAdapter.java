package me.stank.dbreset;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public interface DBAdapter {
    Set<Table> getAllTables(Connection connection) throws SQLException;

    Set<Relationship> getAllRelationships(Connection connection) throws SQLException;

    void delete(Connection connection, Set<Table> tables, Set<Relationship> cyclicRelationships) throws SQLException;
}
