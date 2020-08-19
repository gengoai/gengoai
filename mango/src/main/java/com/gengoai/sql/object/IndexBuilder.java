package com.gengoai.sql.object;

import com.gengoai.Validation;
import com.gengoai.collection.Lists;
import com.gengoai.sql.SQL;
import com.gengoai.sql.SQLElement;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class IndexBuilder implements Serializable {
   private final String name;
   private final SQLElement table;

   IndexBuilder(String name, SQLElement table) {
      this.name = name;
      this.table = table;
   }


   public Index nonUnique(@NonNull String... columns) {
      Validation.checkArgument(columns.length > 0, "Must define at least one column in the index");
      return new Index(table, name, false, Lists.transform(Arrays.asList(columns), SQL::sql));
   }

   public Index nonUnique(@NonNull SQLElement... columns) {
      Validation.checkArgument(columns.length > 0, "Must define at least one column in the index");
      return new Index(table, name, false, Arrays.asList(columns));
   }

   public Index nonUnique(@NonNull List<SQLElement> columns) {
      Validation.checkArgument(columns.size() > 0, "Must define at least one column in the index");
      return new Index(table, name, false, columns);
   }


   public Index unique(@NonNull String... columns) {
      Validation.checkArgument(columns.length > 0, "Must define at least one column in the index");
      return new Index(table, name, true, Lists.transform(Arrays.asList(columns), SQL::sql));
   }

   public Index unique(@NonNull SQLElement... columns) {
      Validation.checkArgument(columns.length > 0, "Must define at least one column in the index");
      return new Index(table, name, true, Arrays.asList(columns));
   }


   public Index unique(@NonNull List<SQLElement> columns) {
      Validation.checkArgument(columns.size() > 0, "Must define at least one column in the index");
      return new Index(table, name, true, columns);
   }

}//END OF IndexBuilder
