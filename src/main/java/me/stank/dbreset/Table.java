package me.stank.dbreset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Table {
    private final String schema;
    private final String name;

    public Table(String schema, String name) {
        this.schema = schema;
        this.name = name;
    }

    public static Set<Table> fromResultSet(ResultSet rs) throws SQLException {
        Set<Table> tables = new HashSet<>();
        while (rs.next()) {
            tables.add(new Table(rs.getString(1), rs.getString(2)));
        }

        return tables;
    }

    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public String getEscapedName(char escapeChar) {
        return schema == null
                ? escapeChar + name + escapeChar
                : escapeChar + schema + escapeChar + '.' + escapeChar + name + escapeChar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return Objects.equals(schema, table.schema) &&
                name.equals(table.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, name);
    }
}
