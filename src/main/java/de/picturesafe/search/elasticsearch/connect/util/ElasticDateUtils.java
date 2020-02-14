/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRules;
import java.util.Date;

public class ElasticDateUtils {

    private ElasticDateUtils() {
    }

    /**
     * Formats a date object to an ISO-860 compliant string.
     *
     * @param date      Date to format
     * @param timeZone  Time zone to apply
     * @return          Foramted date string
     */
    public static String formatIso(Date date, String timeZone) {
        final ZoneId timeZoneId = ZoneId.of(timeZone);
        final ZonedDateTime zonedDateTime = (date instanceof java.sql.Date)
                ? ZonedDateTime.of(((java.sql.Date) date).toLocalDate(), LocalTime.MIDNIGHT, timeZoneId)
                : ZonedDateTime.ofInstant(date.toInstant(), timeZoneId);
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime);
    }

    /**
     * Parses a date in ISO-860 format.
     *
     * @param dateStr   Date in ISO-860 format
     * @return          Date object
     */
    public static Date parseIso(String dateStr) {
        final ZonedDateTime dateTime = ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return Date.from(dateTime.toInstant());
    }

    /**
     * Gets the offset of a time zone for a specific date.
     *
     * @param timeZone  Time zone to get the offset of
     * @param date      Date to calculate the offset for (could vary depending on daylight saving time)
     * @return          Time zone offzet
     */
    public static String getOffset(String timeZone, Date date) {
        final ZoneId timeZoneId = ZoneId.of(timeZone);
        final ZoneRules zoneRules = timeZoneId.getRules();
        return zoneRules.getOffset(date.toInstant()).getId();
    }
}
