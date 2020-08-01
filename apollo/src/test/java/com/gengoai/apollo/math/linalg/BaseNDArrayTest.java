package com.gengoai.apollo.math.linalg;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author David B. Bracewell
 */
public abstract class BaseNDArrayTest {

   final NDArray v1;
   final NDArray v2;
   final NDArray v3;
   final NDArray m1;
   final NDArray m2;
   final NDArrayFactory factory;

   public BaseNDArrayTest(NDArrayFactory factory) {
      this.factory = factory;
      v1 = factory.array(1, 4, new double[]{0, 1, 4, 3});
      v2 = factory.array(1, 4, new double[]{1, 2, 0, 4});
      v3 = factory.array(3, 1, new double[]{1, 2, 4});
      m1 = factory.array(3, 4, new double[]{1.0, 2.0, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
      m2 = factory.array(4, 3, new double[]{1.0, 2.0, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
   }

   @Test
   public void add() throws Exception {
      assertEquals(factory.array(1, 4, new double[]{1.0, 2.0, 5.0, 4.0}), v1.add(1));
      assertEquals(factory.array(1, 4, new double[]{1.0, 2.0, 5.0, 4.0}), v1.copy().addi(1));
      assertEquals(factory.array(1, 4, new double[]{1.0, 3.0, 4.0, 7.0}), v1.add(v2));

      assertEquals(factory.array(1, 4, new double[]{1.0, 3.0, 4.0, 7.0}), v1.copy().addi(v2));
      assertEquals(
            factory.array(3, 4, new double[]{2.0, 7.0, 12.0, 6.0, 11.0, 16.0, 10.0, 15.0, 20.0, 14.0, 19.0, 24.0}),
            m1.add(m2.T()));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 2.0, 3.0, 5.0, 6.0, 7.0, 11.0, 12.0, 13.0, 13.0, 14.0, 15.0}),
            m1.addRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 2.0, 3.0, 5.0, 6.0, 7.0, 11.0, 12.0, 13.0, 13.0, 14.0, 15.0}),
            m1.copy().addiRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{2.0, 4.0, 7.0, 5.0, 7.0, 10.0, 8.0, 10.0, 13.0, 11.0, 13.0, 16.0}),
            m1.addColumnVector(v3));
      assertEquals(
            factory.array(3, 4, new double[]{2.0, 4.0, 7.0, 5.0, 7.0, 10.0, 8.0, 10.0, 13.0, 11.0, 13.0, 16.0}),
            m1.copy().addiColumnVector(v3));

   }

   @Test
   public void copy() throws Exception {
      assertEquals(v1, v1.copy());
      assertEquals(m1, m1.copy());
   }

   @Test
   public void diag() throws Exception {
      assertEquals(factory.array(4, 4)
                          .set(1, 1, 1)
                          .set(2, 2, 4)
                          .set(3, 3, 3),
                   v1.diag()
                  );

      assertEquals(factory.array(4, 4)
                          .set(1, 1, 1)
                          .set(2, 2, 4)
                          .set(3, 3, 3),
                   factory.array(4, 4)
                          .set(1, 1, 1)
                          .set(2, 2, 4)
                          .set(3, 3, 3).diag()
                  );
   }

   @Test
   public void div() throws Exception {
      assertEquals(factory.array(1, 4, new double[]{0.0, 1.0, 4.0, 3.0}), v1.div(1));
      assertEquals(factory.array(1, 4, new double[]{0.0, 1.0, 4.0, 3.0}), v1.copy().divi(1));
      assertEquals(factory.array(1, 4, new double[]{0.0, 0.5, Double.POSITIVE_INFINITY, 0.75}), v1.div(v2));
      assertEquals(factory.array(1, 4, new double[]{0.0, 0.5, Double.POSITIVE_INFINITY, 0.75}), v1.copy().divi(v2));
      assertEquals(
            factory.array(3, 4,
                          new double[]{1.0, 0.4, 0.3333333333333333, 2.0, 0.8333333333333334, 0.6, 2.3333333333333335, 1.1428571428571428, 0.8181818181818182, 2.5, 1.375, 1.0}),
            m1.div(m2.T()));
      assertEquals(
            factory.array(3, 4,
                          new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 4.0, 5.0, 6.0, 1.75, 2.0, 2.25, 3.3333333333333335, 3.6666666666666665, 4.0}),
            m1.divRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 1.0, 0.75, 4.0, 2.5, 1.5, 7.0, 4.0, 2.25, 10.0, 5.5, 3.0}),
            m1.divColumnVector(v3));
      assertEquals(
            factory.array(3, 4,
                          new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 4.0, 5.0, 6.0, 1.75, 2.0, 2.25, 3.3333333333333335, 3.6666666666666665, 4.0}),
            m1.copy().diviRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 1.0, 0.75, 4.0, 2.5, 1.5, 7.0, 4.0, 2.25, 10.0, 5.5, 3.0}),
            m1.copy().diviColumnVector(v3));
   }

   @Test
   public void dot() throws Exception {
      assertEquals(14d, v1.dot(v2), 0d);
      assertEquals(584d, m1.dot(m2.T()), 0);
   }

   @Test
   public void getAndSetVector() throws Exception {
      NDArray v = v1.getRow(0);
      v.setRow(0, factory.array(1, 4, new double[]{1, 2, 0, 4}));
      assertEquals(v2, v);

      v = m1.getColumn(1);
      assertEquals(factory.array(3, 1, new double[]{4.0, 5.0, 6.0}), v);
      NDArray m3 = m1.copy();
      m3.setColumn(2, factory.array(3, 1, new double[]{4.0, 5.0, 6.0}));
      assertEquals(factory.array(3, 4, new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 4.0, 5.0, 6.0, 10.0, 11.0, 12.0}),
                   m3);
   }

   @Test
   public void minMax() throws Exception {
      double max = v1.max();
      NDArray index = v1.rowArgmaxs();
      assertEquals(4d, max, 0d);
      assertEquals(2, index.scalar(), 0d);
      double min = v1.min();
      NDArray mins = v1.rowArgmins();
      assertEquals(0d, min, 0d);

      max = m1.max();
      index = m1.columnArgmaxs();
      assertEquals(12d, max, 0d);
      assertEquals(NDArrayFactory.ND.rowVector(new double[]{2, 2, 2, 2}), index);

      min = m1.min();
      mins = m1.columnArgmins();
      assertEquals(1, min, 0d);
      assertEquals(factory.array(1, 4), mins.slice(0));

   }

   @Test
   public void mmul() throws Exception {
      assertEquals(
            factory.array(3, 3, new double[]{70.0, 80.0, 90.0, 158.0, 184.0, 210.0, 246.0, 288.0, 330.0}), m1.mmul(m2));
   }

   @Test
   public void mul() throws Exception {
      assertEquals(factory.array(1, 4, new double[]{0.0, 1.0, 4.0, 3.0}), v1.mul(1));
      assertEquals(factory.array(1, 4, new double[]{0.0, 1.0, 4.0, 3.0}), v1.copy().muli(1));
      assertEquals(factory.array(1, 4, new double[]{0.0, 2.0, 0.0, 12.0}), v1.mul(v2));
      assertEquals(factory.array(1, 4, new double[]{0.0, 2.0, 0.0, 12.0}), v1.copy().muli(v2));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 10.0, 27.0, 8.0, 30.0, 60.0, 21.0, 56.0, 99.0, 40.0, 88.0, 144.0}),
            m1.mul(m2.T()));
      assertEquals(
            factory.array(3, 4, new double[]{0.0, 0.0, 0.0, 4.0, 5.0, 6.0, 28.0, 32.0, 36.0, 30.0, 33.0, 36.0}),
            m1.mulRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 4.0, 12.0, 4.0, 10.0, 24.0, 7.0, 16.0, 36.0, 10.0, 22.0, 48.0}),
            m1.mulColumnVector(v3));
      assertEquals(
            factory.array(3, 4, new double[]{0.0, 0.0, 0.0, 4.0, 5.0, 6.0, 28.0, 32.0, 36.0, 30.0, 33.0, 36.0}),
            m1.copy().muliRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 4.0, 12.0, 4.0, 10.0, 24.0, 7.0, 16.0, 36.0, 10.0, 22.0, 48.0}),
            m1.copy().muliColumnVector(v3));
   }

   @Test
   public void pow() throws Exception {
      assertEquals(factory.ones(1, 4), v1.map(x -> Math.pow(x, 0)));
      assertEquals(factory.ones(1, 4), v1.copy().mapi(x -> Math.pow(x, 0)));
      assertEquals(v1, v1.map(x -> Math.pow(x, 1)));
      assertEquals(v1, v1.copy().mapi(x -> Math.pow(x, 1)));
      assertEquals(factory.array(1, 4, new double[]{0.0, 1.0, 16.0, 9.0}), v1.map(x -> Math.pow(x, 2)));
      assertEquals(factory.array(1, 4, new double[]{0.0, 1.0, 16.0, 9.0}), v1.copy().mapi(x -> Math.pow(x, 2)));
   }

   @Test
   public void rdiv() throws Exception {
      assertEquals(factory.array(1, 4, new double[]{Double.POSITIVE_INFINITY, 1.0, 0.25, 0.3333333333333333}),
                   v1.rdiv(1));
      assertEquals(factory.array(1, 4, new double[]{Double.POSITIVE_INFINITY, 1.0, 0.25, 0.3333333333333333}),
                   v1.copy().rdivi(1));
      assertEquals(factory.array(1, 4, new double[]{0.0, 0.5, Double.POSITIVE_INFINITY, 0.75}), v2.rdiv(v1));
      assertEquals(factory.array(1, 4, new double[]{0.0, 0.5, Double.POSITIVE_INFINITY, 0.75}), v2.copy().rdivi(v1));
      assertEquals(
            factory.array(3, 4,
                          new double[]{1.0, 0.4, 0.3333333333333333, 2.0, 0.8333333333333334, 0.6, 2.3333333333333335, 1.1428571428571428, 0.8181818181818182, 2.5, 1.375, 1.0}),
            m2.T().rdiv(m1));
      assertEquals(
            factory.array(3, 4,
                          new double[]{0.0, 0.0, 0.0, 0.25, 0.2, 0.16666666666666666, 0.5714285714285714, 0.5, 0.4444444444444444, 0.3, 0.2727272727272727, 0.25}),
            m1.rdivRowVector(v1));
      assertEquals(
            factory.array(3, 4,
                          new double[]{1.0, 1.0, 1.3333333333333333, 0.25, 0.4, 0.6666666666666666, 0.14285714285714285, 0.25, 0.4444444444444444, 0.1, 0.18181818181818182, 0.3333333333333333}),
            m1.rdivColumnVector(v3));
      assertEquals(
            factory.array(3, 4,
                          new double[]{0.0, 0.0, 0.0, 0.25, 0.2, 0.16666666666666666, 0.5714285714285714, 0.5, 0.4444444444444444, 0.3, 0.2727272727272727, 0.25}),
            m1.copy().rdiviRowVector(v1));
      assertEquals(
            factory.array(3, 4,
                          new double[]{1.0, 1.0, 1.3333333333333333, 0.25, 0.4, 0.6666666666666666, 0.14285714285714285, 0.25, 0.4444444444444444, 0.1, 0.18181818181818182, 0.3333333333333333}),
            m1.copy().rdiviColumnVector(v3));
   }

   @Test
   public void rsub() throws Exception {
      assertEquals(factory.array(1, 4, new double[]{1.0, 0.0, -3.0, -2.0}), v1.rsub(1));
      assertEquals(factory.array(1, 4, new double[]{1.0, 0.0, -3.0, -2.0}), v1.copy().rsubi(1));
      assertEquals(factory.array(1, 4, new double[]{-1.0, -1.0, 4.0, -1.0}), v2.copy().rsubi(v1));
      assertEquals(
            factory.array(3, 4, new double[]{0.0, -3.0, -6.0, 2.0, -1.0, -4.0, 4.0, 1.0, -2.0, 6.0, 3.0, 0.0}),
            m2.T().rsub(m1));
      assertEquals(
            factory.array(3, 4, new double[]{-1.0, -2.0, -3.0, -3.0, -4.0, -5.0, -3.0, -4.0, -5.0, -7.0, -8.0, -9.0}),
            m1.rsubRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{0.0, 0.0, 1.0, -3.0, -3.0, -2.0, -6.0, -6.0, -5.0, -9.0, -9.0, -8.0}),
            m1.rsubColumnVector(v3));
      assertEquals(
            factory.array(3, 4, new double[]{-1.0, -2.0, -3.0, -3.0, -4.0, -5.0, -3.0, -4.0, -5.0, -7.0, -8.0, -9.0}),
            m1.copy().rsubiRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{0.0, 0.0, 1.0, -3.0, -3.0, -2.0, -6.0, -6.0, -5.0, -9.0, -9.0, -8.0}),
            m1.copy().rsubiColumnVector(v3));
   }

   @Test
   public void setAndGet() throws Exception {
      double value = v1.get(0);
      assertEquals(0, value, 0d);
      v1.set(0, 20);
      assertEquals(20, v1.get(0), 0d);
      v1.set(0, 0f);
      assertEquals(0, v1.get(0, 0), 0d);

      v1.set(0, 0, 10f);
      v1.set(0, 0, 0f);

      value = m1.get(0, 0);
      assertEquals(1, value, 0d);
      m1.set(0, 0, 20);
      assertEquals(20, m1.get(0, 0), 0d);
      m1.set(0, 0, 1f);
      assertEquals(1, m1.get(0, 0), 0d);

      m1.set(0, 0, 100f);
      m1.set(0, 0, 1);
   }

   @Test
   public void slice() throws Exception {
      assertEquals(factory.scalar(0).scalar(), v1.slice(0).scalar(), 0d);
      assertEquals(factory.array(1, 4, new double[]{1.0, 4.0, 7.0, 10.0}), m1.getSubMatrix(0, 1, 0, 4));
      assertEquals(factory.array(1, 4, new double[]{1.0, 4.0, 7.0, 10.0}), m1.getRow(0));
   }

   @Test
   public void stats() throws Exception {
      assertEquals(8, v1.sum(), 0d);
      assertEquals(factory.scalar(8), v1.rowSums());
      assertEquals(factory.array(1, 4, new double[]{6, 15, 24, 33}), m1.columnSums());
   }

   @Test
   public void sub() throws Exception {
      assertEquals(factory.array(1, 4, new double[]{-1.0, 0.0, 3.0, 2.0}), v1.sub(1));
      assertEquals(factory.array(1, 4, new double[]{-1.0, 0.0, 3.0, 2.0}), v1.copy().subi(1));
      assertEquals(factory.array(1, 4, new double[]{-1.0, -1.0, 4.0, -1.0}), v1.sub(v2));
      assertEquals(factory.array(1, 4, new double[]{-1.0, -1.0, 4.0, -1.0}), v1.copy().subi(v2));
      assertEquals(
            factory.array(3, 4, new double[]{0.0, -3.0, -6.0, 2.0, -1.0, -4.0, 4.0, 1.0, -2.0, 6.0, 3.0, 0.0}),
            m1.sub(m2.T()));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 2.0, 3.0, 3.0, 4.0, 5.0, 3.0, 4.0, 5.0, 7.0, 8.0, 9.0}),
            m1.subRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{0.0, 0.0, -1.0, 3.0, 3.0, 2.0, 6.0, 6.0, 5.0, 9.0, 9.0, 8.0}),
            m1.subColumnVector(v3));
      assertEquals(
            factory.array(3, 4, new double[]{1.0, 2.0, 3.0, 3.0, 4.0, 5.0, 3.0, 4.0, 5.0, 7.0, 8.0, 9.0}),
            m1.copy().subiRowVector(v1));
      assertEquals(
            factory.array(3, 4, new double[]{0.0, 0.0, -1.0, 3.0, 3.0, 2.0, 6.0, 6.0, 5.0, 9.0, 9.0, 8.0}),
            m1.copy().subiColumnVector(v3));
   }

   @Test
   public void test() throws Exception {
      assertEquals(factory.array(1, 4, new double[]{1, 0, 0, 0}), v1.test(d -> d == 0));
      assertEquals(factory.array(1, 4, new double[]{1, 0, 0, 0}), v1.copy().testi(d -> d == 0));
   }

   @Test
   public void zero() throws Exception {
      assertEquals(0, v1.copy().zero().sum(), 0d);
   }
}// END OF BaseNDArrayTest
