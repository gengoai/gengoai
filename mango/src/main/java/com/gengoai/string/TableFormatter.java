package com.gengoai.string;

import com.gengoai.conversion.Cast;
import com.gengoai.io.resource.Resource;
import com.gengoai.io.resource.StringResource;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * The type Table formatter.
 *
 * @author David B. Bracewell
 */
public class TableFormatter implements Serializable {
   private static final int CONTENT = 0;
   private static final int HEADER = 1;
   private static final int FOOTER = 2;

   private static final int FOUR_CORNER = 0;
   private static final int VERTICAL = 1;
   private static final int HORIZONTAL = 2;
   private static final int MIDDLE_BOTTOM_SEPARATOR = 3;
   private static final int MIDDLE_TOP_SEPARATOR = 4;
   private static final int LEFT_TOP_SEPARATOR = 5;
   private static final int RIGHT_TOP_SEPARATOR = 6;
   private static final int LEFT_BOTTOM_SEPARATOR = 7;
   private static final int RIGHT_BOTTOM_SEPARATOR = 8;
   private static final int MIN_CELL_WIDTH = 5;
   private static final String[][] PATTERNS = {
      {"+", "|", "-", "+", "+", "+", "+", "+", "+"},
      {"+", "|", "=", "+", "+", "+", "+", "+", "+"},
      {"+", "|", "=", "+", "+", "+", "+", "+", "+"}
   };

   private static final DecimalFormat longNumberFormatter = new DecimalFormat("0.0E0");
   private static final long serialVersionUID = 1L;
   private List<List<Object>> content = new LinkedList<>();
   private List<List<Object>> footer = new LinkedList<>();
   private List<Object> header = new ArrayList<>();
   private int longestCell = 2;
   private int longestRow;
   private DecimalFormat normalNumberFormatter = new DecimalFormat("0.000");
   private String title;


   private void bar(PrintStream stream, String right, String horizontal, String fourCorner, String left) {
      stream.print(right);
      stream.printf("%s", Strings.repeat(horizontal, longestCell));
      for (int i = 1; i < longestRow; i++) {
         stream.print(fourCorner);
         stream.printf("%s", Strings.repeat(horizontal, longestCell));
      }
      stream.println(left);
   }

   private void bottomBar(PrintStream stream, int type) {
      bar(stream,
          PATTERNS[type][RIGHT_BOTTOM_SEPARATOR],
          PATTERNS[type][HORIZONTAL],
          PATTERNS[type][MIDDLE_BOTTOM_SEPARATOR],
          PATTERNS[type][LEFT_BOTTOM_SEPARATOR]
         );
   }

   /**
    * Clear.
    */
   public void clear() {
      this.header.clear();
      this.content.clear();
      this.longestCell = 2;
      this.longestRow = 0;
      this.title = null;
   }

   /**
    * Content table formatter.
    *
    * @param collection the collection
    * @return the table formatter
    */
   public TableFormatter content(Collection<?> collection) {
      this.content.add(new ArrayList<>(collection));
      longestCell = (int) Math.max(longestCell, collection.stream()
                                                          .mapToDouble(o -> {
                                                                          if (o instanceof Number) {
                                                                             return Math.max(MIN_CELL_WIDTH, length(Cast
                                                                                                                       .as(o)));
                                                                          } else {
                                                                             return Math.max(MIN_CELL_WIDTH,
                                                                                             o.toString().length() + 2);
                                                                          }
                                                                       }
                                                                      ).max().orElse(0)
                                  );
      longestRow = Math.max(longestRow, collection.size());
      return this;
   }

   private String convert(Object o, int longestCell) {
      if (o instanceof Number) {
         Number number = Cast.as(o);
         if (number instanceof Long || number instanceof Integer || number instanceof Short) {
            String numString = Long.toString(number.longValue());
            return numString.length() <= longestCell ? numString : longNumberFormatter.format(number);
         } else {
            String numString = normalNumberFormatter.format(number);
            return numString.length() <= longestCell ? numString : longNumberFormatter.format(number);
         }
      }
      return Strings.abbreviate(o.toString(), longestCell - 2);
   }

   public TableFormatter footer(Collection<?> collection) {
      this.footer.add(new ArrayList<>(collection));
      longestCell = (int) Math.max(longestCell, collection.stream()
                                                          .mapToDouble(o -> {
                                                                          if (o instanceof Number) {
                                                                             return Math.max(MIN_CELL_WIDTH, length(Cast.as(o)));
                                                                          } else {
                                                                             return Math.max(MIN_CELL_WIDTH,
                                                                                             o.toString().length() + 2);
                                                                          }
                                                                       }
                                                                      ).max().orElse(0)
                                  );
      longestRow = Math.max(longestRow, collection.size());
      return this;
   }

   /**
    * Header table formatter.
    *
    * @param collection the collection
    * @return the table formatter
    */
   public <T> TableFormatter header(Collection<? extends T> collection) {
      header.addAll(collection);
      longestCell = (int) Math.max(longestCell, collection.stream()
                                                          .mapToDouble(o -> o.toString().length() + 2)
                                                          .max()
                                                          .orElse(0));
      return this;
   }

   private int length(Number number) {
      if (number instanceof Long || number instanceof Integer || number instanceof Short) {
         return Math.min(longNumberFormatter.format(number).length(), number.toString().length());
      }
      return Math.max(5,
                      Math.min(longNumberFormatter.format(number).length(),
                               normalNumberFormatter.format(number).length()));
   }

   private void middleBar(PrintStream stream, int type) {
      bar(stream,
          PATTERNS[type][VERTICAL],
          PATTERNS[type][HORIZONTAL],
          PATTERNS[type][FOUR_CORNER],
          PATTERNS[type][VERTICAL]
         );
   }

   /**
    * Print the table to the give PrintStream .
    *
    * @param stream the print stream to write to
    */
   public void print(PrintStream stream) {
      longestRow = Math.max(longestRow, header.size());

      if (!Strings.isNullOrBlank(title)) {
         stream.println(Strings.center(title, (longestCell * longestRow) + longestRow + 1));
      }

      if (header.size() > 0) {
         topBar(stream, HEADER);
         printRow(stream, header, longestCell, longestRow, 1);
         middleBar(stream, 1);
      } else {
         topBar(stream, CONTENT);
      }

      for (int r = 0; r < content.size(); r++) {
         printRow(stream, content.get(r), longestCell, longestRow, 0);
         if (r + 1 < content.size()) {
            middleBar(stream, CONTENT);
         }
      }

      if (!footer.isEmpty()) {
         middleBar(stream, FOOTER);
         for (int r = 0; r < footer.size(); r++) {
            printRow(stream, footer.get(r), longestCell, longestRow, 1);
            if (r + 1 < footer.size()) {
               middleBar(stream, CONTENT);
            }
         }
         bottomBar(stream, FOOTER);
      } else {
         bottomBar(stream, CONTENT);
      }
   }

   private void printRow(PrintStream stream, List<Object> row, int longestCell, int longestRow, int type) {
      while (row.size() < longestRow) {
         row.add(Strings.EMPTY);
      }
      stream.print(PATTERNS[type][VERTICAL]);
      stream.printf("%s", Strings.center(convert(row.get(0), longestCell), longestCell));
      for (int i = 1; i < longestRow; i++) {
         stream.print(PATTERNS[type][VERTICAL]);
         stream.printf("%s", Strings.center(convert(row.get(i), longestCell), longestCell));
      }
      stream.println(PATTERNS[type][VERTICAL]);
   }

   /**
    * Sets min cell width.
    *
    * @param cellWidth the cell width
    */
   public void setMinCellWidth(int cellWidth) {
      this.longestCell = cellWidth;
   }

   /**
    * Sets number formatter.
    *
    * @param decimalFormat the decimal format
    */
   public void setNumberFormatter(DecimalFormat decimalFormat) {
      this.normalNumberFormatter = decimalFormat;
   }

   /**
    * Title table formatter.
    *
    * @param title the title
    * @return the table formatter
    */
   public TableFormatter title(String title) {
      this.title = title;
      return this;
   }

   private void topBar(PrintStream stream, int type) {
      bar(stream,
          PATTERNS[type][RIGHT_TOP_SEPARATOR],
          PATTERNS[type][HORIZONTAL],
          PATTERNS[type][MIDDLE_TOP_SEPARATOR],
          PATTERNS[type][LEFT_TOP_SEPARATOR]
         );
   }

   /**
    * Writes the table to a resource.
    *
    * @param resource the resource to write to
    * @return the resource written to
    * @throws IOException Something went wrong writing to the resource
    */
   public Resource write(Resource resource) throws IOException {
      Resource stringResource = new StringResource();
      try (PrintStream printStream = new PrintStream(stringResource.outputStream())) {
         print(printStream);
      }
      resource.write(stringResource.readToString());
      return resource;
   }

}// END OF TableFormatter
