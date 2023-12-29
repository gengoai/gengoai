package com.gengoai.sql;

import lombok.Getter;

public class SQLDataType {
    public enum Affinity{
        TEXT,
        INTEGER,
        REAL,
        NULL,
        BLOB
    }

    public static final SQLDataType SMALL_INTEGER = new SQLDataType("SMALLINT", Affinity.INTEGER);
    public static final SQLDataType INTEGER = new SQLDataType("INTEGER", Affinity.INTEGER);
    public static final SQLDataType BIG_INTEGER = new SQLDataType("BIGINT", Affinity.INTEGER);
    public static final SQLDataType SMALL_SERIAL = new SQLDataType("SMALLSERIAL", Affinity.INTEGER);
    public static final SQLDataType SERIAL = new SQLDataType("SERIAL", Affinity.INTEGER);
    public static final SQLDataType BIG_SERIAL = new SQLDataType("BIGSERIAL", Affinity.INTEGER);
    public static final SQLDataType TIMESTAMP = new SQLDataType("TIMESTAMP", Affinity.TEXT);
    public static final SQLDataType DATE = new SQLDataType("DATE", Affinity.TEXT);
    public static final SQLDataType BLOB = new SQLDataType("BLOB", Affinity.BLOB);
    public static final SQLDataType TEXT = new SQLDataType("TEXT", Affinity.TEXT);
    public static final SQLDataType JSON = new SQLDataType("JSON", Affinity.TEXT);
    public static final SQLDataType BOOLEAN = new SQLDataType("BOOLEAN", Affinity.INTEGER);
    public static final SQLDataType REAL = new SQLDataType("REAL", Affinity.REAL);
    public static final SQLDataType DOUBLE = new SQLDataType("DOUBLE", Affinity.REAL);

    public static SQLDataType VARCHAR(int length) {
        return new SQLDataType("VARCHAR(" + length + ")", Affinity.TEXT);
    }

    public static SQLDataType CHAR(int length) {
        return new SQLDataType("CHAR(" + length + ")", Affinity.TEXT);
    }

    @Getter
    private final String name;
    @Getter
    private final Affinity affinity;

    public SQLDataType(String name, Affinity affinity) {
        this.name = name;
        this.affinity = affinity;
    }


}//END OF SQLDataType
