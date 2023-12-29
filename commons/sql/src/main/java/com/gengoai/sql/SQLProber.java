package com.gengoai.sql;

import com.gengoai.collection.Iterables;
import com.gengoai.collection.Lists;
import com.gengoai.sql.constraint.Constraint;
import com.gengoai.sql.object.Column;
import com.gengoai.sql.object.Index;
import com.gengoai.sql.object.Table;
import com.gengoai.sql.operator.SQLOperable;
import com.gengoai.string.Strings;
import com.gengoai.tuple.Tuple2;
import lombok.NonNull;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.gengoai.sql.SQL.C;
import static com.gengoai.tuple.Tuples.$;

public class SQLProber {
    private final SQLContext context;
    private final DatabaseMetaData metaData;


    public SQLProber(SQLContext context) throws SQLException {
        this.context = context;
        this.metaData = context.getConnection().getMetaData();
    }

    /**
     * Gets the table constraints for a given table
     *
     * @param table the table
     * @return the table constraints
     * @throws SQLException Something went wrong building the table constraints
     */
    public List<Constraint> getConstraints(@NonNull Table table) throws SQLException {
        return getConstraints(table.getName());
    }

    /**
     * Gets the table constraints for a given table
     *
     * @param table the table
     * @return the table constraints
     * @throws SQLException Something went wrong building the table constraints
     */
    public List<Constraint> getConstraints(@NonNull String table) throws SQLException {
        List<Constraint> tableConstraints = new ArrayList<>();
        Tuple2<String, List<String>> primaryKey = getPrimaryKey(table);
        if (primaryKey != null && primaryKey.v2.size() > 1) {
            tableConstraints.add(Constraint.constraint(primaryKey.v1).primaryKey(Lists.transform(primaryKey.v2, SQL::C)));
        }
        return tableConstraints;
    }

    private Tuple2<String, List<String>> getPrimaryKey(@NonNull String table) throws SQLException {
        try (ResultSet rs = context.getConnection().getMetaData().getPrimaryKeys(null, null, table)) {
            List<String> columns = new ArrayList<>();
            String name = null;
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
                name = rs.getString("PK_NAME");
            }
            if (columns.size() > 0) {
                return $(name, columns);
            }
        }
        return null;
    }

    /**
     * Gets the list of columns (non-virtual) for the given table. Tries to infer the constraints on the columns and
     * whether or not the column is a primary key. In some cases the constraints will not be retrieved on the column, but
     * will be retrievable through a call to {@link #getTableIndices(Table)}.
     *
     * @param table the table name
     * @return the table columns
     * @throws SQLException Something went wrong learning about the table
     */
    public List<Column> getStoredColumns(@NonNull Table table) throws SQLException {
        return getStoredColumns(table.getName());
    }

    /**
     * Gets the list of columns (non-virtual) for the given table. Tries to infer the constraints on the columns and
     * whether or not the column is a primary key. In some cases the constraints will not be retrieved on the column, but
     * will be retrievable through a call to {@link #getTableIndices(Table)}.
     *
     * @param table the table name
     * @return the table columns
     * @throws SQLException Something went wrong learning about the table
     */
    public List<Column> getStoredColumns(@NonNull String table) throws SQLException {
        List<Column> columns = new ArrayList<>();
        Tuple2<String, List<String>> primaryKey = getPrimaryKey(table);
        String pk = Strings.EMPTY;
        if (primaryKey != null && primaryKey.v2.size() == 1) {
            pk = Iterables.getFirst(primaryKey.v2, Strings.EMPTY);
        }
        try (ResultSet rs = metaData.getColumns(null, null, table, null)) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String typeName = rs.getString("TYPE_NAME");
                //TODO FIX THIS
                Column column = SQL.column(name, new SQLDataType(typeName, SQLDataType.Affinity.NULL));
                if (!rs.getString("IS_NULLABLE").equalsIgnoreCase("YES")) {
                    column.notNull();
                }
                if (rs.getString("IS_AUTOINCREMENT").equalsIgnoreCase("YES")) {
                    column.autoIncrement();
                }
                String generated = rs.getString("IS_GENERATEDCOLUMN");
                if (Strings.isNotNullOrBlank(generated)) {
                    column.asStored(SQL.sql(generated));
                }
                if (name.equalsIgnoreCase(pk)) {
                    column.primaryKey();
                }

                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * Reverse engineers a {@link Table} definition from the database.
     *
     * @param name the name of the table to reverse engineer
     * @return the reverse engineered table
     * @throws SQLException Something went wrong building the table
     */
    public Table getTable(@NonNull String name) throws SQLException {
        List<Column> columns = new ArrayList<>();
        columns.addAll(getStoredColumns(name));
        columns.addAll(getVirtualColumns(name));
        return new Table(name, null, null, columns, getConstraints(name));
    }

    /**
     * Retrieves the indices on table
     *
     * @param table the table whose indices we want
     * @return the list of {@link Index} on the table
     * @throws SQLException something happened trying to query for table indices
     */
    public List<Index> getTableIndices(@NonNull Table table) throws SQLException {
        return getTableIndices(table.getName());
    }

    /**
     * Retrieves the indices on table
     *
     * @param table the table whose indices we want
     * @return the list of {@link Index} on the table
     * @throws SQLException something happened trying to query for table indices
     */
    public List<Index> getTableIndices(@NonNull String table) throws SQLException {
        List<Index> indices = new ArrayList<>();
        try (ResultSet rs = metaData.getIndexInfo(null, null, table, false, false)) {
            List<SQLElement> columns = new ArrayList<>();
            String name = null;
            boolean isUnique = false;
            while (rs.next()) {
                String column = rs.getString("COLUMN_NAME");
                int ordinal = rs.getInt("ORDINAL_POSITION");
                if (ordinal == 1 && columns.size() > 0) {
                    indices.add(new Index(SQL.sql(table),
                                          name,
                                          isUnique,
                                          columns));
                    columns.clear();
                }
                name = rs.getString("INDEX_NAME");
                isUnique = rs.getInt("NON_UNIQUE") == 0;
                SQLOperable c = C(column);
                if (Strings.safeEquals("A", rs.getString("ASC_OR_DESC"), false)) {
                    c = c.asc();
                } else if (Strings.safeEquals("D", rs.getString("ASC_OR_DESC"), false)) {
                    c = c.desc();
                }
                columns.add(c);
            }
            if (columns.size() > 0) {
                indices.add(new Index(SQL.sql(table),
                                      name,
                                      isUnique,
                                      columns));
            }
        }
        return indices;
    }

    /**
     * Gets a list of the table names in the database
     *
     * @return the table names
     * @throws SQLException Something went wrong getting the table names
     */
    public List<String> getTableNames() throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    private List<Column> getVirtualColumns(@NonNull String table, @NonNull List<Column> columns) throws SQLException {
        List<Column> virtualColumns = new ArrayList<>();
        try (ResultSet rs = SQL.query("SELECT * FROM " + table).query(context)) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String name = metaData.getColumnName(i);
                String type = metaData.getColumnTypeName(i);
                if (columns.stream().noneMatch(c -> SQL.columnNamesEqual(c.getName(), name))) {
                    //TODO: Fix this
                    virtualColumns.add(SQL.column(name, new SQLDataType(type, SQLDataType.Affinity.NULL)).asVirtual(SQL.sql("?")));
                }
            }
        }
        return virtualColumns;
    }

    /**
     * Attempts to the get the virtual columns for a given table. Note that it may not be possible to retrieve the
     * generated as statement that accompanies the virtual column.
     *
     * @param table the table name
     * @return the virtual columns
     * @throws SQLException Something went wrong getting the virtual names
     */
    public List<Column> getVirtualColumns(@NonNull Table table) throws SQLException {
        return getVirtualColumns(table.getName(), table.getColumns());
    }

    /**
     * Attempts to the get the virtual columns for a given table. Note that it may not be possible to retrieve the
     * generated as statement that accompanies the virtual column.
     *
     * @param table the table name
     * @return the virtual columns
     * @throws SQLException Something went wrong getting the virtual names
     */
    public List<Column> getVirtualColumns(@NonNull String table) throws SQLException {
        return getVirtualColumns(table, getStoredColumns(table));
    }

}//END OF SQLProber
