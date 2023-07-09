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

package com.gengoai.hermes;


import com.gengoai.annotation.Preload;

/**
 * Predefined set of common entities.
 *
 * @author David B. Bracewell
 */
@Preload
public interface Entities {
    //-----------------------------------------------------------------------------------------
    // DATE_TIME ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType DATE_TIME = EntityType.make("DATE_TIME");
    EntityType DATE = EntityType.make(DATE_TIME, "DATE");
    EntityType TIME = EntityType.make(DATE_TIME, "TIME");
    //    EntityType ERA = EntityType.make(DATE_TIME, "ERA");
    EntityType PERIOD = EntityType.make(DATE_TIME, "PERIOD");
    EntityType TIME_PERIOD = EntityType.make(PERIOD, "TIME_PERIOD");
    EntityType DATE_PERIOD = EntityType.make(PERIOD, "DATE_PERIOD");
//    EntityType WEEK_PERIOD = EntityType.make(PERIOD, "WEEK_PERIOD");
//    EntityType MONTH_PERIOD = EntityType.make(PERIOD, "MONTH_PERIOD");
//    EntityType YEAR_PERIOD = EntityType.make(PERIOD, "YEAR_PERIOD");

    //-----------------------------------------------------------------------------------------
    // INTERNET ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType INTERNET = EntityType.make("INTERNET");
    EntityType EMOTICON = EntityType.make(INTERNET, "EMOTICON");
    EntityType HASH_TAG = EntityType.make(INTERNET, "HASH_TAG");

    //-----------------------------------------------------------------------------------------
    // LOCATION ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType LOCATION = EntityType.make("LOCATION");

    EntityType GPE = EntityType.make(LOCATION, "GPE");
//   EntityType CITY = EntityType.make(GPE, "CITY");
//   EntityType COUNTY = EntityType.make(GPE, "COUNTY");
//   EntityType PROVINCE = EntityType.make(GPE, "PROVINCE");
//   EntityType COUNTRY = EntityType.make(GPE, "COUNTRY");
//   EntityType REGION = EntityType.make(LOCATION, "REGION");
//   EntityType CONTINENTAL_REGION = EntityType.make(REGION, "CONTINENTAL_REGION");
//   EntityType DOMESTIC_REGION = EntityType.make(REGION, "DOMESTIC_REGION");

    EntityType GEOLOGICAL_REGION = EntityType.make(LOCATION, "GEOLOGICAL_REGION");
    EntityType LANDFORM = EntityType.make(GEOLOGICAL_REGION, "LANDFORM");
    EntityType WATER_FORM = EntityType.make(GEOLOGICAL_REGION, "WATER_FORM");
//   EntityType SEA = EntityType.make(GEOLOGICAL_REGION, "SEA");


    EntityType ASTRAL_BODY = EntityType.make(LOCATION, "ASTRAL_BODY");
    EntityType STAR = EntityType.make(ASTRAL_BODY, "STAR");
    EntityType PLANET = EntityType.make(ASTRAL_BODY, "PLANET");
    EntityType CONSTELLATION = EntityType.make(ASTRAL_BODY, "CONSTELLATION");

//    EntityType RELATIVE_LOCATION = EntityType.make(LOCATION, "RELATIVE_LOCATION");

    EntityType ADDRESS = EntityType.make(LOCATION, "ADDRESS");
    EntityType POSTAL_ADDRESS = EntityType.make(ADDRESS, "POSTAL_ADDRESS");
    EntityType PHONE_NUMBER = EntityType.make(ADDRESS, "PHONE_NUMBER");
    EntityType REPLY = EntityType.make(ADDRESS, "REPLY");
    EntityType EMAIL = EntityType.make(ADDRESS, "EMAIL");
    EntityType URL = EntityType.make(ADDRESS, "URL");

    //-----------------------------------------------------------------------------------------
    // FACILITY ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType FACILITY = EntityType.make("FACILITY");

    EntityType FACILITY_PART = EntityType.make(FACILITY, "FACILITY_PART");

    EntityType GOE = EntityType.make(FACILITY, "GOE");
    EntityType PUBLIC_INSTITUTION = EntityType.make(GOE, "PUBLIC_INSTITUTION");
    EntityType SCHOOL = EntityType.make(GOE, "SCHOOL");
    EntityType RESEARCH_INSTITUTE = EntityType.make(GOE, "RESEARCH_INSTITUTE");
    EntityType MUSEUM = EntityType.make(GOE, "MUSEUM");
    EntityType MARKET = EntityType.make(GOE, "MARKET");
    EntityType PARK = EntityType.make(GOE, "PARK");
    EntityType ZOO = EntityType.make(GOE, "ZOO");
    EntityType THEATER = EntityType.make(GOE, "THEATER");
    EntityType WORSHIP_PLACE = EntityType.make(GOE, "WORSHIP_PLACE");
    EntityType AMUSEMENT_PARK = EntityType.make(GOE, "AMUSEMENT_PARK");
    EntityType AIRPORT = EntityType.make(GOE, "AIRPORT");
    EntityType STATION = EntityType.make(GOE, "STATION");
    EntityType PORT = EntityType.make(GOE, "PORT");
    EntityType CAR_STOP = EntityType.make(GOE, "CAR_STOP");

    EntityType LINE = EntityType.make(FACILITY, "LINE");
    EntityType RAILROAD = EntityType.make(LINE, "RAILROAD");
    EntityType ROAD = EntityType.make(LINE, "ROAD");
    EntityType WATERWAY = EntityType.make(LINE, "WATERWAY");
    EntityType TUNNEL = EntityType.make(LINE, "TUNNEL");
    EntityType BRIDGE = EntityType.make(LINE, "BRIDGE");

    //-----------------------------------------------------------------------------------------
    // NUMBER ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType NUMBER = EntityType.make("NUMBER");
    EntityType CARDINAL = EntityType.make(NUMBER, "CARDINAL");
    EntityType ORDINAL = EntityType.make(NUMBER, "ORDINAL");
    //    EntityType POINT = EntityType.make(NUMBER, "POINT");
    EntityType MONEY = EntityType.make(NUMBER, "MONEY");
    //    EntityType MULTIPLICATION = EntityType.make(NUMBER, "MULTIPLICATION");
    EntityType PERCENT = EntityType.make(NUMBER, "PERCENT");
    EntityType FREQUENCY = EntityType.make(NUMBER, "FREQUENCY");
    //    EntityType RANK = EntityType.make(NUMBER, "RANK");
    EntityType AGE = EntityType.make(NUMBER, "AGE");
    EntityType QUANTITY = EntityType.make(NUMBER, "QUANTITY");
    EntityType MEASUREMENT = EntityType.make(NUMBER, "MEASUREMENT");

    //-----------------------------------------------------------------------------------------
    // MEASUREMENT ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    // Moved to Number
//    EntityType PHYSICAL_EXTENT = EntityType.make(MEASUREMENT, "PHYSICAL_EXTENT");
//    EntityType SPACE = EntityType.make(MEASUREMENT, "SPACE");
//    EntityType VOLUME = EntityType.make(MEASUREMENT, "VOLUME");
//    EntityType WEIGHT = EntityType.make(MEASUREMENT, "WEIGHT");
//    EntityType SPEED = EntityType.make(MEASUREMENT, "SPEED");
//    EntityType INTENSITY = EntityType.make(MEASUREMENT, "INTENSITY");
//    EntityType TEMPERATURE = EntityType.make(MEASUREMENT, "TEMPERATURE");
//    EntityType CALORIE = EntityType.make(MEASUREMENT, "CALORIE");
//    EntityType SEISMIC_INTENSITY = EntityType.make(MEASUREMENT, "SEISMIC_INTENSITY");

    //-----------------------------------------------------------------------------------------
    // ORGANIZATION ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType ORGANIZATION = EntityType.make("ORGANIZATION");

    //    EntityType INTERNATIONAL_ORGANIZATION = EntityType.make(ORGANIZATION, "INTERNATIONAL_ORGANIZATION");
//    EntityType SHOW_ORGANIZATION = EntityType.make(ORGANIZATION, "SHOW_ORGANIZATION");
//    EntityType FAMILY = EntityType.make(ORGANIZATION, "FAMILY");
//    EntityType ETHNIC_GROUP = EntityType.make(ORGANIZATION, "ETHNIC_GROUP");
//    EntityType NATIONALITY = EntityType.make(ETHNIC_GROUP, "NATIONALITY");

    EntityType SPORTS_ORGANIZATION = EntityType.make(ORGANIZATION, "SPORTS_ORGANIZATION");
    EntityType PRO_SPORTS_ORGANIZATION = EntityType.make(SPORTS_ORGANIZATION, "PRO_SPORTS_ORGANIZATION");
    EntityType SPORTS_LEAGUE = EntityType.make(SPORTS_ORGANIZATION, "SPORTS_LEAGUE");

//   EntityType CORPORATION = EntityType.make(ORGANIZATION, "CORPORATION");
//   EntityType COMPANY = EntityType.make(CORPORATION, "COMPANY");
//   EntityType COMPANY_GROUP = EntityType.make(CORPORATION, "COMPANY_GROUP");

    EntityType POLITICAL_ORGANIZATION = EntityType.make(ORGANIZATION, "POLITICAL_ORGANIZATION");
    EntityType GOVERNMENT = EntityType.make(POLITICAL_ORGANIZATION, "GOVERNMENT");
    EntityType POLITICAL_PARTY = EntityType.make(POLITICAL_ORGANIZATION, "POLITICAL_PARTY");
    EntityType MILITARY = EntityType.make(POLITICAL_ORGANIZATION, "MILITARY");


    //-----------------------------------------------------------------------------------------
    // PERSON ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType PERSON = EntityType.make("PERSON");
    EntityType PERSON_GROUP = EntityType.make(PERSON, "PERSON_GROUP");

    EntityType TITLE = EntityType.make(PERSON, "TITLE");
    EntityType POSITION_TITLE = EntityType.make(TITLE, "POSITION_TITLE");
//   EntityType OCCUPATION = EntityType.make(PERSON, "OCCUPATION");


    //-----------------------------------------------------------------------------------------
    // PRODUCT ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType PRODUCT = EntityType.make("PRODUCT");
    EntityType WORK_OF_ART = EntityType.make(PRODUCT, "WORK_OF_ART");
    EntityType RULE = EntityType.make(PRODUCT, "RULE");
    EntityType LAW = EntityType.make(RULE, "LAW");
    EntityType TREATY = EntityType.make(RULE, "TREATY");
    EntityType LANGUAGE = EntityType.make(PRODUCT, "LANGUAGE");
    EntityType VEHICLE = EntityType.make(PRODUCT, "VEHICLE");
    EntityType LAND_VEHICLE = EntityType.make(VEHICLE, "LAND_VEHICLE");
    EntityType AIR_VEHICLE = EntityType.make(VEHICLE, "AIR_VEHICLE");
    EntityType WATER_VEHICLE = EntityType.make(VEHICLE, "WATER_VEHICLE");
    EntityType SPACE_VEHICLE = EntityType.make(VEHICLE, "SPACE_VEHICLE");

    //-----------------------------------------------------------------------------------------
    // EVENT ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType EVENT = EntityType.make("EVENT");

    //-----------------------------------------------------------------------------------------
    // MISC ENTITY TYPES
    //-----------------------------------------------------------------------------------------
    EntityType MISC = EntityType.make("MISC");

}//END OF Entities
