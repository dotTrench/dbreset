package me.stank.dbreset;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static me.stank.dbreset.TestHelper.count;
import static me.stank.dbreset.TestHelper.execute;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

interface DBAdapterTest {
    DBAdapter adapter();

    Connection connection() throws SQLException;

    @Test
    default void shouldDeleteData() throws Exception {
        Connection connection = connection();
        Checkpoint checkpoint = new Checkpoint(adapter());

        runMigration(connection, "shouldDeleteData.sql");

        PreparedStatement prepared = connection.prepareStatement("insert into foo(value) VALUES (?)");
        for (int i = 0; i < 100; i++) {
            prepared.setInt(1, i);
            prepared.addBatch();
        }

        prepared.executeBatch();

        assertEquals(100, count(connection, "foo"));

        checkpoint.reset(connection);

        assertEquals(0, count(connection, "foo"));
    }

    @Test
    default void shouldHandleRelationships() throws Exception {
        Connection connection = connection();
        runMigration(connection, "shouldHandleRelationships.sql");

        PreparedStatement foo = connection.prepareStatement("insert into foo(value) VALUES (?)");
        PreparedStatement baz = connection.prepareStatement(" insert into baz(value, foovalue) VALUES(?, ?)");
        for (int i = 0; i < 100; i++) {
            foo.setInt(1, i);
            foo.addBatch();
            baz.setInt(1, i);
            baz.setInt(2, i);
            baz.addBatch();
        }

        foo.executeBatch();
        baz.executeBatch();

        assertAll(
                () -> assertEquals(100, count(connection, "foo")),
                () -> assertEquals(100, count(connection, "baz"))
        );

        new Checkpoint(adapter()).reset(connection);

        assertAll(
                () -> assertEquals(0, count(connection, "foo")),
                () -> assertEquals(0, count(connection, "baz"))
        );
    }

    @Test
    default void shouldHandleCircularRelationships() throws Exception {
        Connection connection = connection();

        runMigration(connection, "shouldHandleCircularRelationships.sql");


        PreparedStatement child = connection.prepareStatement("insert into child VALUES (?, null)");
        PreparedStatement parent = connection.prepareStatement(" insert into parent VALUES(?, null)");

        for (int i = 0; i < 100; i++) {
            child.setInt(1, i);
            child.addBatch();
            parent.setInt(1, i);
            parent.addBatch();
        }

        parent.executeBatch();
        child.executeBatch();

        connection.createStatement().execute("update parent set childid = 0");
        connection.createStatement().execute("update child set parentid = 0");

        assertAll(
                () -> assertEquals(100, count(connection, "parent")),
                () -> assertEquals(100, count(connection, "child"))
        );
        new Checkpoint(adapter()).reset(connection);

        assertEquals(0, count(connection, "parent"));
        assertEquals(0, count(connection, "child"));
    }

    @Test
    default void shouldHandleComplexCycles() throws Exception {
        Connection connection = connection();
        runMigration(connection, "shouldHandleComplexCycles.sql");
        execute(connection, "insert into d (id) values (1)");
        execute(connection, "insert into c (id, d_id) values (1, 1)");
        execute(connection, "insert into a (id) values (1)");
        execute(connection, "insert into b (id, c_id, d_id) values (1, 1, 1)");
        execute(connection, "insert into e (id, a_id) values (1, 1)");
        execute(connection, "insert into f (id, b_id) values (1, 1)");
        execute(connection, "update a set b_id = 1");
        execute(connection, "update b set a_id = 1");

        assertAll(
                () -> assertEquals(1, count(connection, "a")),
                () -> assertEquals(1, count(connection, "b")),
                () -> assertEquals(1, count(connection, "c")),
                () -> assertEquals(1, count(connection, "d")),
                () -> assertEquals(1, count(connection, "e")),
                () -> assertEquals(1, count(connection, "f"))
        );
        new Checkpoint(adapter()).reset(connection);
        assertAll(
                () -> assertEquals(0, count(connection, "a")),
                () -> assertEquals(0, count(connection, "b")),
                () -> assertEquals(0, count(connection, "c")),
                () -> assertEquals(0, count(connection, "d")),
                () -> assertEquals(0, count(connection, "e")),
                () -> assertEquals(0, count(connection, "f"))
        );
    }

    @Test
    default void shouldHandleSelfRelationships() throws Exception {
        Connection connection = connection();
        runMigration(connection, "shouldHandleSelfRelationShips.sql");

        PreparedStatement batch = connection.prepareStatement("insert into circle values(?, ?)");
        batch.setInt(1, 1);
        batch.setNull(2, Types.INTEGER);
        batch.addBatch();

        for (int i = 1; i < 100; i++) {
            batch.setInt(1, i + 1);
            batch.setInt(2, i);
            batch.addBatch();
        }

        batch.executeBatch();

        assertEquals(100, count(connection, "circle"));

        new Checkpoint(adapter()).reset(connection);

        assertEquals(0, count(connection, "circle"));
    }


    default void runMigration(Connection connection, String name) throws Exception {
        TestHelper.runMigration(connection, name);
    }
}