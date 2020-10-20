package com.gengoai.sql.constraint;

import com.gengoai.collection.Lists;
import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLElement;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface ConstraintBuilder {


   default CheckConstraint check(String sql) {
      return new CheckConstraint(getName(), SQL.sql(sql));
   }

   default CheckConstraint check(@NonNull SQLElement sql) {
      return new CheckConstraint(getName(), sql);
   }

   default ForeignKeyConstraint foreignKey(@NonNull List<SQLElement> columns,
                                           @NonNull String foreignTable,
                                           @NonNull List<SQLElement> foreignTableColumns) {
      return new ForeignKeyConstraint(getName(), columns, SQL.sql(foreignTable), foreignTableColumns);
   }

   default ForeignKeyConstraint foreignKey(@NonNull List<SQLElement> columns,
                                           @NonNull SQLElement foreignTable,
                                           @NonNull List<SQLElement> foreignTableColumns) {
      return new ForeignKeyConstraint(getName(), columns, foreignTable, foreignTableColumns);
   }

   default ForeignKeyConstraint foreignKey(@NonNull Collection<String> columns,
                                           @NonNull String foreignTable,
                                           @NonNull Collection<String> foreignTableColumns) {
      return new ForeignKeyConstraint(getName(),
                                      columns.stream().map(SQL::C).collect(Collectors.toList()),
                                      SQL.sql(foreignTable),
                                      foreignTableColumns.stream().map(SQL::C).collect(Collectors.toList()));
   }

   default ForeignKeyConstraint foreignKey(@NonNull Collection<String> columns,
                                           @NonNull SQLElement foreignTable,
                                           @NonNull Collection<String> foreignTableColumns) {
      return new ForeignKeyConstraint(getName(),
                                      columns.stream().map(SQL::C).collect(Collectors.toList()),
                                      foreignTable,
                                      foreignTableColumns.stream().map(SQL::C).collect(Collectors.toList()));
   }

   String getName();

   default NotNullConstraint notNull(@NonNull String column) {
      return new NotNullConstraint(getName(), SQL.C(column));
   }

   default NotNullConstraint notNull(@NonNull SQLElement column) {
      return new NotNullConstraint(getName(), column);
   }

   default PrimaryKeyConstraint primaryKey(@NonNull String... columns) {
      return new PrimaryKeyConstraint(getName(), Lists.transform(Arrays.asList(columns), SQL::C));
   }

   default PrimaryKeyConstraint primaryKey(@NonNull SQLElement... columns) {
      return new PrimaryKeyConstraint(getName(), Arrays.asList(columns));
   }

   default PrimaryKeyConstraint primaryKey(Collection<SQLElement> columns) {
      return new PrimaryKeyConstraint(getName(), columns);
   }

   default UniqueConstraint unique(@NonNull String... columns) {
      return new UniqueConstraint(getName(), Lists.transform(Arrays.asList(columns), SQL::C));
   }

   default UniqueConstraint unique(@NonNull SQLElement... columns) {
      return new UniqueConstraint(getName(), Arrays.asList(columns));
   }

   default UniqueConstraint unique(Collection<SQLElement> columns) {
      return new UniqueConstraint(getName(), columns);
   }


}//END OF ConstraintBuilder
