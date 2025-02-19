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

package com.gengoai.apollo.math.measure;

import com.gengoai.Lazy;
import com.gengoai.Validation;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;

/**
 * <p>Common methods for calculating the correlation between arrays of values.</p>
 *
 * @author David B. Bracewell
 */
public enum Correlation implements CorrelationMeasure {
   /**
    * <a href="https://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient">The Pearson product-moment
    * correlation coefficient.</a>
    */
   Pearson {
      @Override
      public double calculate(double[] x, double[] y) {
         Validation.checkArgument(x.length == y.length,
                                  "Dimension mismatch dim(x)=" + x.length + " != dim(y)=" + y.length);
         double x2 = 0d;
         double y2 = 0d;
         double sumX = 0d;
         double sumY = 0d;
         double xy = 0d;
         for (int i = 0; i < x.length; i++) {
            sumX += x[i];
            x2 += x[i] * x[i];
            sumY += y[i];
            y2 += y[i] * y[i];
            xy += x[i] * y[i];
         }
         double SSx = x2 - ((sumX * sumX) / x.length);
         double SSy = y2 - ((sumY * sumY) / x.length);
         double SSxy = xy - ((sumY * sumX) / x.length);
         return SSxy / Math.sqrt(SSx * SSy);
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient">Spearman's rank correlation
    * coefficient</a>
    */
   Spearman {
      final transient Lazy<SpearmansCorrelation> spearmansCorrelation = new Lazy<>(SpearmansCorrelation::new);

      @Override
      public double calculate(double[] v1, double[] v2) {
         Validation.checkArgument(v1.length == v2.length,
                                  "Vector dimension mismatch " + v1.length + " != " + v2.length);
         Validation.checkArgument(v1.length >= 2, "Need at least two elements");
         return spearmansCorrelation.get().correlation(v1, v2);
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Kendall_rank_correlation_coefficient"> Kendall's Tau-b rank
    * correlation</a>
    */
   Kendall {
      final transient Lazy<KendallsCorrelation> kendallsCorrelation = new Lazy<>(KendallsCorrelation::new);

      @Override
      public double calculate(double[] v1, double[] v2) {
         return kendallsCorrelation.get().correlation(v1, v2);
      }
   },
   /**
    * <a href="https://en.wikipedia.org/wiki/Coefficient_of_determination">R^2 Coefficient of determination</a>
    */
   R_Squared {
      @Override
      public double calculate(double[] v1, double[] v2) {
         return FastMath.pow(Pearson.calculate(v1, v2), 2d);
      }
   },
   AUC {
      @Override
      public double calculate(double[] x, double[] y) {
         Arrays.sort(x);
         Arrays.sort(y);

         int n0 = x.length;
         int n1 = y.length;

         int i0 = 0, i1 = 0;
         int rank = 1;
         double sum = 0d;

         while (i0 < n0 && i1 < n1) {
            double v0 = x[i0];
            double v1 = y[i1];

            if (v0 < v1) {
               i0++;
               rank++;
            } else if (v1 < v0) {
               i1++;
               sum += rank;
               rank++;
            } else {
               int k0 = 0;
               while (i0 < n0 && x[i0] == v0) {
                  k0++;
                  i0++;
               }


               int k1 = 0;
               while (i1 < n1 && y[i1] == v0) {
                  k1++;
                  i1++;
               }


               sum += (rank + (k0 + k1 - 1) / 2.0) * k1;
               rank += k0 + k1;
            }
         }

         if (i1 < n1) {
            sum += (rank + (n1 - i1 - 1) / 2.0) * (n1 - i1);
         }


         return (sum / n1 - (n1 + 1.0) / 2.0) / n0;
      }
   }


}//END OF Correlation
