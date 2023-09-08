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

package com.gengoai.news;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class DateParser {
    private List<DateFormat> formats = new ArrayList<>();
    private Pattern yyyyMMdd_dash = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private Pattern yyyyMMdd_slash = Pattern.compile("\\d{4}/\\d{2}/\\d{2}");

    public DateParser(Locale locale) {
        formats.add(DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                   DateFormat.FULL,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                   DateFormat.LONG,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                   DateFormat.MEDIUM,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                   DateFormat.SHORT,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                   DateFormat.DEFAULT,
                                                   locale));

        formats.add(DateFormat.getDateTimeInstance(DateFormat.LONG,
                                                   DateFormat.FULL,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.LONG,
                                                   DateFormat.LONG,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.LONG,
                                                   DateFormat.MEDIUM,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.LONG,
                                                   DateFormat.SHORT,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.LONG,
                                                   DateFormat.DEFAULT,
                                                   locale));

        formats.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                   DateFormat.FULL,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                   DateFormat.LONG,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                   DateFormat.MEDIUM,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                   DateFormat.SHORT,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                   DateFormat.DEFAULT,
                                                   locale));

        formats.add(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                   DateFormat.FULL,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                   DateFormat.LONG,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                   DateFormat.MEDIUM,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                   DateFormat.SHORT,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                   DateFormat.DEFAULT,
                                                   locale));

        formats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                                                   DateFormat.FULL,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                                                   DateFormat.LONG,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                                                   DateFormat.MEDIUM,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                                                   DateFormat.SHORT,
                                                   locale));
        formats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                                                   DateFormat.DEFAULT,
                                                   locale));

        formats.add(DateFormat.getDateInstance(DateFormat.FULL, locale));
        formats.add(DateFormat.getDateInstance(DateFormat.LONG, locale));
        formats.add(DateFormat.getDateInstance(DateFormat.MEDIUM, locale));
        formats.add(DateFormat.getDateInstance(DateFormat.SHORT, locale));
        formats.add(DateFormat.getDateInstance(DateFormat.DEFAULT, locale));
        formats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public LocalDateTime parse(String string) {
        if (yyyyMMdd_dash.matcher(string).matches()) {
            TemporalAccessor ta = DateTimeFormatter.ofPattern("yyyy-MM-dd").parse(string);
            return LocalDate.from(ta).atTime(12, 0);
        }
        if (yyyyMMdd_slash.matcher(string).matches()) {
            TemporalAccessor ta = DateTimeFormatter.ofPattern("yyyy/MM/dd").parse(string);
            return LocalDate.from(ta).atTime(12, 0);
        }

        try {
            return LocalDateTime.parse(string);
        } catch (DateTimeParseException e) {
            for (DateFormat format : formats) {
                try {
                    return format.parse(string).toInstant()
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDateTime();
                } catch (ParseException parseException) {
                    //pass
                }
            }
        }
        return null;
    }

}//END OF DateParser
