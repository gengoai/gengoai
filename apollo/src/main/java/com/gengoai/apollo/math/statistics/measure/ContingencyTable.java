/*
 * (c) 2005 David B. Bracewell
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.gengoai.apollo.math.statistics.measure;


import com.gengoai.tuple.Tuple2;

/**
 * <p>Matrix for storing frequency distribution of variables and used for calculating various affinities and performing
 * statistical tests. </p>
 *
 * @author David B. Bracewell
 */
public class ContingencyTable {

   private final int numberOfRows;
   private final int numberOfColumns;
   private final double[][] table;
   private double sum = 0;
   private double[] rowSums;
   private double[] columnSums;

   /**
    * Instantiates a new Contingency table.
    *
    * @param numberOfRows    the numberOfRows
    * @param numberOfColumns the numberOfColumns
    */
   public ContingencyTable(int numberOfRows, int numberOfColumns) {
      this.numberOfRows = numberOfRows;
      this.numberOfColumns = numberOfColumns;
      this.table = new double[numberOfRows][numberOfColumns];
      this.rowSums = new double[numberOfRows];
      this.columnSums = new double[numberOfColumns];
   }

   /**
    * <pre>
    * 			 | word2 | not-word2|
    * 	       --------------------
    *     word1 |  n11  |   n12    | n1p
    * not-word1 |  n21  |   n22    | n2p
    * 	         --------------------
    * 			    np1      np2      npp
    *
    * </pre>
    *
    * @param n11 the count 1 and 2
    * @param n1p the count 1
    * @param np1 the count 2
    * @param npp the total
    * @return the contingency table
    */
   public static ContingencyTable create2X2(double n11, double n1p, double np1, double npp) {
      ContingencyTable table = new ContingencyTable(2, 2);
      table.set(0, 0, n11);
      table.set(0, 1, n1p - n11);
      table.set(1, 0, np1 - n11);
      table.set(1, 1, npp - n1p - np1 + n11);
      return table;
   }

   /**
    * * <pre>
    * 			           | word3 | not-word3|
    *           	         --------------------
    *     word1 word2     |  n111 |   n11x   |
    *     word1 not-word2 |  n1x1 |   n1xx   |
    *    not-word1 word2  |  nx11 |   nx1x   |
    * not-word1 not-word2 |  nxx1 |   nxxx   |
    *                     --------------------
    * </pre>
    *
    * @param n111   the n 111
    * @param n11x   the n 11 x
    * @param n1x1   the n 1 x 1
    * @param nx11   the nx 11
    * @param count1 the count 1
    * @param count2 the count 2
    * @param count3 the count 3
    * @param total  the total
    * @return the contingency table
    */
   public static ContingencyTable create3x3(Number n111,
                                            Number n11x,
                                            Number n1x1,
                                            Number nx11,
                                            Number count1,
                                            Number count2,
                                            Number count3,
                                            Number total
                                           ) {
      ContingencyTable table = new ContingencyTable(4, 2);
      table.set(0, 0, n111.longValue()); // w1 + w2 + w3
      table.set(0, 1, n11x.longValue()); // w1 + w2 + !w3
      table.set(1, 0, n1x1.longValue());// w1 + !w2 + w3
      table.set(1, 1, count1.longValue() - n111.longValue()); // w1 + !w2 + !w3
      table.set(2, 0, nx11.longValue());// !w1 + w2 + w3
      table.set(2, 1, count2.longValue() - n111.longValue()); // !w1 + w2 + !w3
      table.set(3, 0, count3.longValue() - n111.longValue());// !w1 + !w2 + w3
      table.set(3, 1,
                total.longValue() - (count1.longValue() + count2.longValue() + count3.longValue()));// !w1 + !w2 + !w3
      return table;
   }

   /**
    * <p>Constructs a contingency table explicitly to be used for comparing the frequency of a word between two
    * corpora.</p>
    *
    * @param countInCorpusOne the count in corpus one
    * @param corpusOneSize    the corpus one size
    * @param countInCorpusTwo the count in corpus two
    * @param corpusTwoSize    the corpus two size
    * @return the contingency table
    */
   public static ContingencyTable forCorpusComparison(double countInCorpusOne, double corpusOneSize, double countInCorpusTwo, double corpusTwoSize) {
      ContingencyTable table = new ContingencyTable(2, 2);
      table.set(0, 0, countInCorpusOne);
      table.set(1, 0, corpusOneSize - countInCorpusOne);
      table.set(0, 1, countInCorpusTwo);
      table.set(1, 1, corpusTwoSize - countInCorpusTwo);
      return table;
   }

   /**
    * Gets the number of columns in the table
    *
    * @return the number of columns in the table
    */
   public int columnCount() {
      return numberOfColumns;
   }

   /**
    * Calculates the sum of the given column, which relates to the marginal frequency.
    *
    * @param column the column
    * @return the sum (i.e. marginal frequency) of the column
    */
   public double columnSum(int column) {
      return columnSums[column];
   }

   /**
    * Degrees of freedom double.
    *
    * @return the double
    */
   public double degreesOfFreedom() {
      return (numberOfColumns - 1.0) * (numberOfRows - 1.0);
   }

   /**
    * Gets the obeserved value at the given location in the table
    *
    * @param m the row number
    * @param n the column number
    * @return the observed value in row m, column nu
    */
   public double get(int m, int n) {
      return table[m][n];
   }

   /**
    * Gets the expected count for a cell, which is the sum of the row * the sum of the column divided by the sum of the
    * table
    *
    * @param m The row
    * @param n The column
    * @return The expected count
    */
   public double getExpected(int m, int n) {
      return (rowSums[m] * columnSums[n]) / sum;
   }

   /**
    * Gets sum.
    *
    * @return the sum
    */
   public double getSum() {
      return sum;
   }

   /**
    * Calculates the row and column sums
    *
    * @return A pair (row first, column second) of sums
    */
   public Tuple2<double[], double[]> rowColumnSums() {
      return Tuple2.of(rowSums, columnSums);
   }

   /**
    * Gets the number of rows in the table
    *
    * @return the number of rows in the table
    */
   public int rowCount() {
      return numberOfRows;
   }


   /**
    * Calculates the sum of the given row, which relates to the marginal frequency.
    *
    * @param row the row
    * @return the sum (i.e. marginal frequency) of the row
    */
   public double rowSum(int row) {
      return rowSums[row];
   }

   /**
    * Set void.
    *
    * @param row    the m
    * @param column the n
    * @param val    the val
    */
   public void set(int row, int column, double val) {
      sum -= table[row][column];
      rowSums[row] -= table[row][column];
      columnSums[column] -= table[row][column];
      table[row][column] = val;
      sum += val;
      rowSums[row] += val;
      columnSums[column] += val;
   }

   /**
    * Gets the raw data in the table
    *
    * @return The table as an array
    */
   public double[][] toArray() {
      return table;
   }


}//END OF ContingencyTable
