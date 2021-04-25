/*
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
 */

package com.gengoai.lucene;

import com.gengoai.Validation;
import com.gengoai.conversion.Converter;
import com.gengoai.conversion.TypeConversionException;
import lombok.NonNull;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;

/**
 * <p></p>
 *
 * @author David B. Bracewell
 */
public enum FieldType {
   TEXT {
      @Override
      public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored) {
         if (value instanceof CharSequence) {
            document.add(new TextField(fieldName, value.toString(), stored));
         } else {
            try {
               document.add(new TextField(fieldName, Converter.convert(value, String.class), stored));
            } catch (TypeConversionException e) {
               throw new RuntimeException(e);
            }
         }
      }

      @Override
      public Analyzer getAnalyzer() {
         return new StandardAnalyzer();
      }
   },
   STRING {
      @Override
      public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored) {
         if (value instanceof CharSequence) {
            document.add(new StringField(fieldName, value.toString(), stored));
         } else {
            try {
               document.add(new StringField(fieldName, Converter.convert(value, String.class), stored));
            } catch (TypeConversionException e) {
               throw new RuntimeException(e);
            }
         }
      }
   },
   LONG {
      @Override
      public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored) {
         try {
            Long number = Converter.convert(value, Long.class);
            document.add(new LongPoint(fieldName, number));
            if (stored == Field.Store.YES) {
               document.add(new StoredField(fieldName + "_stored", number));
            }
         } catch (TypeConversionException e) {
            throw new RuntimeException(e);
         }
      }
   },
   DOUBLE {
      @Override
      public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored) {
         try {
            Double number = Converter.convert(value, Double.class);
            document.add(new DoublePoint(fieldName, number));
            if (stored == Field.Store.YES) {
               document.add(new StoredField(fieldName + "_stored", number));
            }
         } catch (TypeConversionException e) {
            throw new RuntimeException(e);
         }
      }
   },
   INTEGER {
      @Override
      public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored) {
         try {
            Integer number = Converter.convert(value, Integer.class);
            document.add(new IntPoint(fieldName, number));
            if (stored == Field.Store.YES) {
               document.add(new StoredField(fieldName + "_stored", number));
            }
         } catch (TypeConversionException e) {
            throw new RuntimeException(e);
         }
      }
   },
   FLOAT {
      @Override
      public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored) {
         try {
            Float number = Converter.convert(value, Float.class);
            document.add(new FloatPoint(fieldName, number));
            if (stored == Field.Store.YES) {
               document.add(new StoredField(fieldName + "_stored", number));
            }
         } catch (TypeConversionException e) {
            throw new RuntimeException(e);
         }
      }
   },
   BOOLEAN {
      @Override
      public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored) {
         try {
            Boolean number = Converter.convert(value, Boolean.class);
            document.add(new StringField(fieldName, number.toString(), stored));
         } catch (TypeConversionException e) {
            throw new RuntimeException(e);
         }
      }
   },
   STORED {
      @Override
      public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored) {
         Validation.checkArgument(stored == Field.Store.YES, "Attempting to set a STORED field to Not Store its value");
         if (value instanceof Double) {
            document.add(new StoredField(fieldName, (Double) value));
         } else if (value instanceof Long) {
            document.add(new StoredField(fieldName, (Long) value));
         } else if (value instanceof Integer) {
            document.add(new StoredField(fieldName, (Integer) value));
         } else if (value instanceof Float) {
            document.add(new StoredField(fieldName, (Float) value));
         } else if (value instanceof byte[]) {
            document.add(new StoredField(fieldName, (byte[]) value));
         } else if (value instanceof CharSequence) {
            document.add(new StoredField(fieldName, value.toString()));
         } else {
            try {
               document.add(new StoredField(fieldName, Converter.convert(value, String.class)));
            } catch (TypeConversionException e) {
               throw new RuntimeException(e);
            }
         }
      }
   };
//   DATE,
//   TIME,
//   DATE_TIME,
//   EMBEDDING,
//   BINARY,
//   GEOSPATIAL;

   public static void main(String[] args) {
      FieldType fieldType = FieldType.TEXT;
      Document document = new Document();
      fieldType.addMultiValueField(document, "str", new String[]{"A", "B"}, Field.Store.NO);
      System.out.println(document);
   }

   public void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value) {
      addField(document, fieldName, value, Field.Store.YES);
   }

   public abstract void addField(@NonNull Document document, @NonNull String fieldName, @NonNull Object value, @NonNull Field.Store stored);

   public void addMultiValueField(@NonNull Document document, @NonNull String fieldName, @NonNull long[] values, @NonNull Field.Store stored) {
      for (Object value : values) {
         addField(document, fieldName, value, stored);
      }
   }

   public void addMultiValueField(@NonNull Document document, @NonNull String fieldName, @NonNull int[] values, @NonNull Field.Store stored) {
      for (Object value : values) {
         addField(document, fieldName, value, stored);
      }
   }

   public void addMultiValueField(@NonNull Document document, @NonNull String fieldName, @NonNull double[] values, @NonNull Field.Store stored) {
      for (Object value : values) {
         addField(document, fieldName, value, stored);
      }
   }

   public void addMultiValueField(@NonNull Document document, @NonNull String fieldName, @NonNull float[] values, @NonNull Field.Store stored) {
      for (Object value : values) {
         addField(document, fieldName, value, stored);
      }
   }

   public void addMultiValueField(@NonNull Document document, @NonNull String fieldName, @NonNull boolean[] values, @NonNull Field.Store stored) {
      for (Object value : values) {
         addField(document, fieldName, value, stored);
      }
   }

   public void addMultiValueField(@NonNull Document document, @NonNull String fieldName, @NonNull short[] values, @NonNull Field.Store stored) {
      for (Object value : values) {
         addField(document, fieldName, value, stored);
      }
   }

   public void addMultiValueField(@NonNull Document document, @NonNull String fieldName, @NonNull Iterable<? extends Object> values, @NonNull Field.Store stored) {
      for (Object value : values) {
         addField(document, fieldName, value, stored);
      }
   }

   public void addMultiValueField(@NonNull Document document, @NonNull String fieldName, @NonNull Object[] values, @NonNull Field.Store stored) {
      for (Object value : values) {
         addField(document, fieldName, value, stored);
      }
   }

   public Analyzer getAnalyzer() {
      return new KeywordAnalyzer();
   }

}//END OF FieldType
