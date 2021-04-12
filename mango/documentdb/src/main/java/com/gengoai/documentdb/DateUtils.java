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

package com.gengoai.documentdb;

import com.gengoai.Language;
import com.gengoai.string.Strings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * <p>Various utilities for parsing and working with dates.</p>
 *
 * @author David B. Bracewell
 */
public final class DateUtils {

  private DateUtils() {
  }

  public final static long MILLISECONDS_PER_SECOND = 1000;
  public final static long MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60;
  public final static long MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60;
  public final static long MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;

  public final static SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");
  public final static SimpleDateFormat US_STANDARD = new SimpleDateFormat("MM/dd/yyyy");

  /**
   * <p>
   * Determines the fractional difference between two dates at a given granularity.
   * </p>
   *
   * @param start       The start date
   * @param end         The end date
   * @param MILLI_PER_X The number of milliseconds in the time granularity we want to convert to
   * @return The fractional difference at the given granularity
   */
  public static double difference(Date start, Date end, double MILLI_PER_X) {
    if (start == null || end == null) {
      throw new NullPointerException();
    }
    if (MILLI_PER_X <= 0) {
      throw new IllegalArgumentException("The MILLI_PER_X argument must be greater than 0");
    }
    return (double) (end.getTime() - start.getTime()) / MILLI_PER_X;
  }

  /**
   * <p>
   * Determines the difference between two dates at a given granularity using
   * {@link TimeUnit#convert(long, TimeUnit)}.
   * </p>
   *
   * @param start       The start date
   * @param end         The end date
   * @param granularity The granularity
   * @return The difference at the given granularity
   */
  public static long difference(Date start, Date end, TimeUnit granularity) {
    if (start == null || end == null || granularity == null) {
      throw new NullPointerException();
    }
    return granularity.convert((end.getTime() - start.getTime()), TimeUnit.MILLISECONDS);
  }

  /**
   * <p>
   * Determines the fractional difference between two dates at a given granularity.
   * </p>
   *
   * @param start       The start date
   * @param end         The end date
   * @param MILLI_PER_X The number of milliseconds in the time granularity we want to convert to
   * @return The fractional difference at the given granularity
   */
  public static double difference(java.sql.Date start, java.sql.Date end, double MILLI_PER_X) {
    if (start == null || end == null) {
      throw new NullPointerException();
    }
    if (MILLI_PER_X <= 0) {
      throw new IllegalArgumentException("The MILLI_PER_X argument must be greater than 0");
    }
    return (double) (end.getTime() - start.getTime()) / MILLI_PER_X;
  }

  /**
   * <p>
   * Determines the difference between two dates at a given granularity using
   * {@link TimeUnit#convert(long, TimeUnit)}.
   * </p>
   *
   * @param start       The start date
   * @param end         The end date
   * @param granularity The granularity
   * @return The difference at the given granularity
   */
  public static long difference(java.sql.Date start, java.sql.Date end, TimeUnit granularity) {
    if (start == null || end == null || granularity == null) {
      throw new NullPointerException();
    }
    return granularity.convert((end.getTime() - start.getTime()), TimeUnit.MILLISECONDS);
  }

  /**
   * <p>
   * Determines the fractional difference between two dates at a given granularity.
   * </p>
   *
   * @param start       The start date (in milliseconds)
   * @param end         The end date (in milliseconds)
   * @param MILLI_PER_X The number of milliseconds in the time granularity we want to convert to
   * @return The fractional difference at the given granularity
   */
  public static double difference(long start, long end, double MILLI_PER_X) {
    if (MILLI_PER_X <= 0) {
      throw new IllegalArgumentException("The MILLI_PER_X argument must be greater than 0");
    }
    return (double) (end - start) / MILLI_PER_X;
  }

  /**
   * <p>
   * Determines the difference between two dates at a given granularity using
   * {@link TimeUnit#convert(long, TimeUnit)}.
   * </p>
   *
   * @param start       The start date (in milliseconds)
   * @param end         The end date (in milliseconds)
   * @param granularity The granularity
   * @return The difference at the given granularity
   */
  public static long difference(long start, long end, TimeUnit granularity) {
    if (granularity == null) {
      throw new NullPointerException();
    }
    return granularity.convert((end - start), TimeUnit.MILLISECONDS);
  }

  /**
   * <p>
   * Determines if c1 is on or after c2, i.e. greater than or equal
   * </p>
   *
   * @param c1 Calendar 1
   * @param c2 Calendar 2
   * @return True if c1 is on or after c2, False otherwise
   */
  public static boolean isOnOrAfter(Calendar c1, Calendar c2) {
    return c1.after(c2) || c1.equals(c2);
  }

  /**
   * <p>
   * Determines if d1 is on or after d2, i.e. greater than or equal
   * </p>
   *
   * @param d1 Date 1
   * @param d2 Date 2
   * @return True if d1 is on or after d2, False otherwise
   */
  public static boolean isOnOrAfter(Date d1, Date d2) {
    return d1.after(d2) || d1.equals(d2);
  }

  /**
   * <p>
   * Determines if c1 is on or before c2, i.e. less than or equal
   * </p>
   *
   * @param c1 Calendar 1
   * @param c2 Calendar 2
   * @return True if c1 is on or before c2, False otherwise
   */
  public static boolean isOnOrBefore(Calendar c1, Calendar c2) {
    return c1.before(c2) || c1.equals(c2);
  }

  /**
   * <p>
   * Determines if d1 is on or before d2, i.e. less than or equal
   * </p>
   *
   * @param d1 Date 1
   * @param d2 Date 2
   * @return True if d1 is on or before d2, False otherwise
   */
  public static boolean isOnOrBefore(Date d1, Date d2) {
    return d1.before(d2) || d1.equals(d2);
  }

  /**
   * <p>
   * Parses a String into a date, ignoring exceptions. If an exception does occur, null is
   * returned.
   * </p>
   *
   * @param date   The date string
   * @param format The DateFormat
   * @return The string as a Date or null
   */
  public static Date parseQuietly(String date, DateFormat format) {
    try {
      return format.parse(date);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * <p>
   * Parses a String in anyone of the standard forms defined by a Locale.
   * </p>
   *
   * @param date   The date to parse
   * @param locale The locale that determines how the date is parsed
   * @return The date or null
   */
  public static Date parseQuietly(String date, Locale locale) {
    DateFormat[] formats = new DateFormat[]{
      SimpleDateFormat.getDateTimeInstance(),
      DateFormat.getDateInstance(DateFormat.SHORT, locale),
      DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
      DateFormat.getDateInstance(DateFormat.LONG, locale),
      DateFormat.getDateInstance(DateFormat.FULL, locale)
    };
    for (DateFormat format : formats) {
      Date rval = parseQuietly(date, format);
      if (rval != null) {
        return rval;
      }
    }
    return null;
  }


  /**
   * <p>
   * Parses a date string in any of the formats using all valid locales for a given language.
   * </p>
   *
   * @param date     The date to parse
   * @param language The language that determines how the date is parsed
   * @return The date or null
   */
  public static Date parseQuietly(String date, Language language) {
    if (Strings.isNullOrBlank(date) || language == null) {
      return null;
    }
    for (Locale locale : language.getLocales()) {
      Date parsedDate = parseQuietly(date, locale);
      if (parsedDate != null) {
        return parsedDate;
      }
    }
    return null;
  }

}//END OF DateUtils
