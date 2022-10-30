package com.gengoai.collection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A table is a two dimensional structure that associates a value with two keys (i.e. a row and column key). A table
 * maybe sparse, meaning not all cells contain values. Methods on a table that work with rows and columns return Map
 * views that when updated will be reflected in table.
 *
 * @param <R> the row type parameter
 * @param <C> the column type parameter
 * @param <V> the value type parameter
 * @author David B. Bracewell
 */
@JsonDeserialize(as = HashBasedTable.class)
public interface Table<R, C, V> {

   /**
    * Clears the table.
    */
   void clear();

   /**
    * Returns a map view for a column in the table
    *
    * @param column the column
    * @return Map of row,value pairs
    */
   Map<R, V> column(C column);

   /**
    * Returns a set of column keys that have one or more values associated
    *
    * @return the set of column keys that have one or more values associated
    */
   Set<C> columnKeySet();

   /**
    * Checks if a value exists for a given row and column
    *
    * @param row    the row
    * @param column the column
    * @return True if it exists, False otherwise
    */
   boolean contains(R row, C column);

   /**
    * Checks if column key exists in the table
    *
    * @param column the column
    * @return True if it exists, False otherwise
    */
   boolean containsColumn(C column);

   /**
    * Checks if row key exists in the table
    *
    * @param row the row
    * @return True if it exists, False otherwise
    */
   boolean containsRow(R row);

   /**
    * Gets the set of entries in the table
    *
    * @return the set of entries
    */
   Set<TableEntry<R, C, V>> entrySet();

   /**
    * Gets the value of the cell for the given row and column or null if not available.
    *
    * @param row    the row
    * @param column the column
    * @return the value of the cell at the given row and column or null
    */
   V get(R row, C column);

   default V getOrDefault(R row, C column, V defaultValue) {
      if(contains(row, column)) {
         return get(row, column);
      }
      return defaultValue;
   }

   /**
    * Sets the value of the cell at the given row and column
    *
    * @param row    the row
    * @param column the column
    * @param value  the value
    * @return the previous value
    */
   V put(R row, C column, V value);

   /**
    * Removes the value at the given cell.
    *
    * @param row    the row
    * @param column the column
    * @return the value of the cell
    */
   V remove(R row, C column);

   /**
    * Removes a column from the table
    *
    * @param column the column
    * @return Map containing row, value pairs
    */
   Map<R, V> removeColumn(C column);

   /**
    * Removes a row from the table
    *
    * @param row the row
    * @return Map containing column, value pairs
    */
   Map<C, V> removeRow(R row);

   /**
    * Returns a map view for a row in the table
    *
    * @param row the row
    * @return Map of column,value pairs
    */
   Map<C, V> row(R row);

   /**
    * Returns a set of row keys that have one or more values associated
    *
    * @return the set of row keys that have one or more values associated
    */
   Set<R> rowKeySet();

   /**
    * The size in number of row and column mappings
    *
    * @return number of row and column mappings
    */
   int size();

   /**
    * Collection of cell values in the table
    *
    * @return the collection of cell values in the table
    */
   Collection<V> values();

}//END OF Table
