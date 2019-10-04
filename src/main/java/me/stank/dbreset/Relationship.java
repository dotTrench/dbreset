package me.stank.dbreset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Relationship {
    private final Table parent;
    private final Table child;
    private final String name;

    public Relationship(Table parent, Table child, String name) {
        this.parent = parent;
        this.child = child;
        this.name = name;
    }

    public static Set<Relationship> fromResultSet(ResultSet rs) throws SQLException {
        HashSet<Relationship> relationships = new HashSet<>();
        while (rs.next()) {
            Table from = new Table(rs.getString(1), rs.getString(2));
            Table to = new Table(rs.getString(3), rs.getString(4));

            String name = rs.getString(5);
            relationships.add(new Relationship(from, to, name));
        }

        return relationships;
    }

    public Table getParent() {
        return parent;
    }

    public Table getChild() {
        return child;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relationship that = (Relationship) o;
        return parent.equals(that.parent) &&
                child.equals(that.child) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, child, name);
    }
}
